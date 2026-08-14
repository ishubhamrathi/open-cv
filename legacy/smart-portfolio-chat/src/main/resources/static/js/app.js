// Global state
let sessionId = 'session_' + Math.random().toString(36).substr(2, 9);
let knowledgeItems = [];

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Load knowledge items for admin panel
    loadKnowledgeItems();
    loadStats();
    
    // Setup navigation
    setupNavigation();
    
    // Setup form submission
    setupFormHandlers();
});

// Navigation
function setupNavigation() {
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const target = this.getAttribute('href');
            
            // Update active state
            navLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
            
            // Show/hide sections
            if (target === '#admin') {
                document.getElementById('chat-section').style.display = 'none';
                document.getElementById('admin-section').style.display = 'block';
                loadKnowledgeItems();
                loadStats();
            } else {
                document.getElementById('chat-section').style.display = 'block';
                document.getElementById('admin-section').style.display = 'none';
            }
        });
    });
}

// Chat functionality
function handleKeyPress(event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
}

async function sendMessage(message = null) {
    const input = document.getElementById('chat-input');
    const messageText = message || input.value.trim();
    
    if (!messageText) return;
    
    // Add user message to chat
    addMessageToChat(messageText, 'user');
    input.value = '';
    
    // Show typing indicator
    showTypingIndicator();
    
    try {
        const response = await fetch('/api/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                message: messageText,
                sessionId: sessionId
            })
        });
        
        if (!response.ok) throw new Error('Failed to get response');
        
        const data = await response.json();
        
        // Remove typing indicator
        removeTypingIndicator();
        
        // Add bot response
        addMessageToChat(data.message, 'bot');
        
        // Update suggestions based on response
        if (data.suggestions && data.suggestions.length > 0) {
            updateSuggestions(data.suggestions);
        }
        
        // Scroll to bottom
        scrollToBottom();
        
    } catch (error) {
        console.error('Error:', error);
        removeTypingIndicator();
        addMessageToChat('Sorry, I encountered an error. Please try again.', 'bot');
    }
}

function sendSuggestion(question) {
    sendMessage(question);
}

function addMessageToChat(content, sender) {
    const messagesContainer = document.getElementById('chat-messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${sender}-message`;
    
    const now = new Date();
    const timeString = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    
    messageDiv.innerHTML = `
        <div class="message-content">${formatMessage(content)}</div>
        <div class="message-time">${timeString}</div>
    `;
    
    messagesContainer.appendChild(messageDiv);
    scrollToBottom();
}

function formatMessage(content) {
    // Convert markdown-like syntax to HTML
    return content
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\n/g, '<br>');
}

function showTypingIndicator() {
    const messagesContainer = document.getElementById('chat-messages');
    const typingDiv = document.createElement('div');
    typingDiv.className = 'message bot-message';
    typingDiv.id = 'typing-indicator';
    
    typingDiv.innerHTML = `
        <div class="message-content">
            <div class="typing-indicator">
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
            </div>
        </div>
    `;
    
    messagesContainer.appendChild(typingDiv);
    scrollToBottom();
}

function removeTypingIndicator() {
    const typingIndicator = document.getElementById('typing-indicator');
    if (typingIndicator) {
        typingIndicator.remove();
    }
}

function scrollToBottom() {
    const messagesContainer = document.getElementById('chat-messages');
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function updateSuggestions(suggestions) {
    const suggestionsContainer = document.getElementById('suggestions');
    suggestionsContainer.innerHTML = suggestions
        .map(s => `<button class="suggestion-btn" onclick="sendSuggestion('${s.replace(/'/g, "\\'")}')">${s}</button>`)
        .join('');
}

// Admin Panel - Tab switching
function showTab(tabName) {
    // Update tab buttons
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
    
    // Update tab content
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    document.getElementById(`${tabName}-tab`).classList.add('active');
    
    if (tabName === 'view') {
        loadKnowledgeItems();
    } else if (tabName === 'stats') {
        loadStats();
    }
}

// Admin Panel - Load Knowledge Items
async function loadKnowledgeItems() {
    try {
        const response = await fetch('/api/knowledge');
        if (!response.ok) throw new Error('Failed to load knowledge items');
        
        knowledgeItems = await response.json();
        renderKnowledgeList(knowledgeItems);
    } catch (error) {
        console.error('Error loading knowledge items:', error);
    }
}

function renderKnowledgeList(items) {
    const container = document.getElementById('knowledge-list');
    
    if (items.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: var(--text-secondary); padding: 40px;">No knowledge items found.</p>';
        return;
    }
    
    container.innerHTML = items.map(item => `
        <div class="knowledge-item" data-id="${item.id}">
            <div class="knowledge-item-header">
                <span class="knowledge-category">${item.category}</span>
                <div class="knowledge-actions">
                    <button class="action-btn edit-btn" onclick="openEditModal(${item.id})">Edit</button>
                    <button class="action-btn delete-btn" onclick="deleteKnowledgeItem(${item.id})">Delete</button>
                </div>
            </div>
            <div class="knowledge-question">${escapeHtml(item.question)}</div>
            <div class="knowledge-answer">${escapeHtml(item.answer.substring(0, 200))}${item.answer.length > 200 ? '...' : ''}</div>
        </div>
    `).join('');
}

function searchKnowledge() {
    const query = document.getElementById('search-input').value.toLowerCase();
    
    if (!query) {
        renderKnowledgeList(knowledgeItems);
        return;
    }
    
    const filtered = knowledgeItems.filter(item => 
        item.question.toLowerCase().includes(query) ||
        item.answer.toLowerCase().includes(query) ||
        item.category.toLowerCase().includes(query)
    );
    
    renderKnowledgeList(filtered);
}

// Admin Panel - Form handling
function setupFormHandlers() {
    // Add/Edit form
    document.getElementById('knowledge-form').addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const formData = {
            category: document.getElementById('category').value,
            question: document.getElementById('question').value,
            answer: document.getElementById('answer').value,
            patterns: document.getElementById('patterns').value.split(',').map(p => p.trim()).filter(p => p),
            tags: document.getElementById('tags').value,
            isActive: true
        };
        
        const editId = document.getElementById('edit-id').value;
        
        try {
            let response;
            if (editId) {
                response = await fetch(`/api/knowledge/${editId}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(formData)
                });
            } else {
                response = await fetch('/api/knowledge', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(formData)
                });
            }
            
            if (!response.ok) throw new Error('Failed to save knowledge item');
            
            alert('Knowledge item saved successfully!');
            resetForm();
            showTab('view');
            loadKnowledgeItems();
            loadStats();
            
        } catch (error) {
            console.error('Error saving knowledge item:', error);
            alert('Failed to save knowledge item. Please try again.');
        }
    });
    
    // Edit modal form
    document.getElementById('edit-form').addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const id = document.getElementById('modal-edit-id').value;
        const formData = {
            category: document.getElementById('modal-category').value,
            question: document.getElementById('modal-question').value,
            answer: document.getElementById('modal-answer').value,
            isActive: true
        };
        
        try {
            const response = await fetch(`/api/knowledge/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData)
            });
            
            if (!response.ok) throw new Error('Failed to update knowledge item');
            
            closeModal();
            alert('Knowledge item updated successfully!');
            loadKnowledgeItems();
            loadStats();
            
        } catch (error) {
            console.error('Error updating knowledge item:', error);
            alert('Failed to update knowledge item. Please try again.');
        }
    });
}

function resetForm() {
    document.getElementById('knowledge-form').reset();
    document.getElementById('edit-id').value = '';
}

// Admin Panel - Edit Modal
function openEditModal(id) {
    const item = knowledgeItems.find(i => i.id === id);
    if (!item) return;
    
    document.getElementById('modal-edit-id').value = item.id;
    document.getElementById('modal-category').value = item.category;
    document.getElementById('modal-question').value = item.question;
    document.getElementById('modal-answer').value = item.answer;
    
    document.getElementById('edit-modal').classList.add('active');
}

function closeModal() {
    document.getElementById('edit-modal').classList.remove('active');
}

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('edit-modal');
    if (event.target === modal) {
        closeModal();
    }
}

// Admin Panel - Delete
async function deleteKnowledgeItem(id) {
    if (!confirm('Are you sure you want to delete this knowledge item?')) return;
    
    try {
        const response = await fetch(`/api/knowledge/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Failed to delete knowledge item');
        
        alert('Knowledge item deleted successfully!');
        loadKnowledgeItems();
        loadStats();
        
    } catch (error) {
        console.error('Error deleting knowledge item:', error);
        alert('Failed to delete knowledge item. Please try again.');
    }
}

// Admin Panel - Statistics
async function loadStats() {
    try {
        const response = await fetch('/api/knowledge/stats');
        if (!response.ok) throw new Error('Failed to load statistics');
        
        const stats = await response.json();
        renderStats(stats);
    } catch (error) {
        console.error('Error loading statistics:', error);
    }
}

function renderStats(stats) {
    const container = document.getElementById('stats-container');
    
    const categoryCounts = stats.categoryCounts || {};
    const categoryCards = Object.entries(categoryCounts).map(([category, count]) => `
        <div class="stat-card">
            <div class="stat-value">${count}</div>
            <div class="stat-label">${category.charAt(0).toUpperCase() + category.slice(1)}</div>
        </div>
    `).join('');
    
    container.innerHTML = `
        <div class="stat-card">
            <div class="stat-value">${stats.totalItems || 0}</div>
            <div class="stat-label">Total Knowledge Items</div>
        </div>
        <div class="stat-card">
            <div class="stat-value">${(stats.categories || []).length}</div>
            <div class="stat-label">Categories</div>
        </div>
        ${categoryCards}
    `;
}

// Utility functions
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
