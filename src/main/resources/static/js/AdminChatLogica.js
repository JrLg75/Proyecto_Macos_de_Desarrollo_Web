// --- Lógica para el WIDGET DE CHAT DE ADMIN ---

// (Es una copia de ChatbotLogica.js, pero con IDs y URLs cambiadas)

function toggleAdminChatbot() {
    const chatWindow = document.getElementById('adminChatWindow');
    const isVisible = chatWindow.style.display === 'flex';
    chatWindow.style.display = isVisible ? 'none' : 'flex';
}

function sendAdminMessage() {
    const userInput = document.getElementById('adminUserInput');
    const message = userInput.value.trim();
    if (message === '') return;

    appendAdminMessage(message, 'user-message');
    userInput.value = '';
    scrollToAdminBottom();

    // Mostramos un indicador de "pensando"
    appendAdminMessage('Analizando datos y generando insight... 🧠', 'ai-message', 'thinking-indicator');

    // Llamar al agente de ADMIN en backend
    fetch('/api/v1/admin-agent/chat', { // <-- URL DEL NUEVO CONTROLLER
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: message, sessionId: 'admin_session' }) // sessionId puede ser fijo
    })
    .then(res => {
        if (!res.ok) throw new Error('Error en la solicitud al agente admin');
        return res.json();
    })
    .then(data => {
        // Quitamos el indicador de "pensando"
        const thinkingIndicator = document.getElementById('thinking-indicator');
        if (thinkingIndicator) {
            thinkingIndicator.parentElement.remove();
        }

        if (data && data.reply) {
            // Usamos innerHTML para renderizar saltos de línea y formato de la IA
            appendAdminMessage(data.reply.replace(/\n/g, '<br>'), 'ai-message');
        } else {
            appendAdminMessage('No pude entender la respuesta del servidor.', 'ai-message');
        }
        scrollToAdminBottom();
    })
    .catch((err) => {
        console.error(err);
        const thinkingIndicator = document.getElementById('thinking-indicator');
        if (thinkingIndicator) {
            thinkingIndicator.parentElement.remove();
        }
        appendAdminMessage('Ocurrió un error al contactar al asistente de IA.', 'ai-message');
        scrollToAdminBottom();
    });
}

function handleAdminKeyPress(event) {
    if (event.key === 'Enter') {
        sendAdminMessage();
    }
}

function appendAdminMessage(text, className, elementId = null) {
    const chatBody = document.getElementById('adminChatBody');
    const msgDiv = document.createElement('div');
    msgDiv.className = `message ${className}`;

    const p = document.createElement('p');
    p.innerHTML = text; // Usamos innerHTML para el <br>

    if (elementId) {
        p.id = elementId;
    }

    msgDiv.appendChild(p);
    chatBody.appendChild(msgDiv);
}

function scrollToAdminBottom() {
    const chatBody = document.getElementById('adminChatBody');
    chatBody.scrollTop = chatBody.scrollHeight;
}