// Función para mostrar/ocultar el widget del chat
function toggleChatbot() {
    const chatWindow = document.getElementById('chatWindow');
    const isVisible = chatWindow.style.display === 'flex';
    chatWindow.style.display = isVisible ? 'none' : 'flex';
}

// Función para enviar el mensaje (punto de integración con Spring Boot)
function sendMessage() {
    const userInput = document.getElementById('userInput');
    const message = userInput.value.trim();

    if (message === '') return;

    // 1. Mostrar mensaje del usuario en el chat
    appendMessage(message, 'user-message');

    // 2. Limpiar el input
    userInput.value = '';

    // 3. SCROLL hacia abajo
    scrollToBottom();

	// Llamar al agente conversacional en backend
	const sessionId = localStorage.getItem('chat_session_id') || (window.crypto && crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).slice(2));
	localStorage.setItem('chat_session_id', sessionId);

	fetch('/api/v1/agent/chat', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({ message, sessionId })
	})
		.then(res => {
			if (!res.ok) throw new Error('Error en la solicitud');
			return res.json();
		})
		.then(data => {
			console.log('[Agent API] Respuesta completa:', data);
			if (data && data.reply) {
				console.log('[Agent API] Texto IA:', data.reply);
				appendMessage(data.reply, 'ai-message');
				scrollToBottom();
			} else {
				appendMessage('No pude entender la respuesta del servidor.', 'ai-message');
			}
			if (data && Array.isArray(data.products)) {
				data.products.forEach(p => {
					appendProductCard({
						id: p.id,
						name: p.name || 'Producto',
						price: p.price || '',
						image: p.imageUrl || 'https://via.placeholder.com/200x200/555/ffffff?text=Producto'
					});
				});
				scrollToBottom();
			}
		})
		.catch(() => {
			appendMessage('Ocurrió un error al contactar al asistente.', 'ai-message');
		});
}

// Manejar la tecla Enter para enviar
function handleKeyPress(event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
}

// Añadir un mensaje al cuerpo del chat
function appendMessage(text, className) {
    const chatBody = document.getElementById('chatBody');
    const msgDiv = document.createElement('div');
    msgDiv.className = `message ${className}`;
    msgDiv.innerHTML = `<p>${text}</p>`;
    chatBody.appendChild(msgDiv);
}

// Añadir una tarjeta de producto (respuesta de la IA)
function appendProductCard(product) {
    const chatBody = document.getElementById('chatBody');
    const cardHtml = `
        <div class="message ai-message">
            <div class="product-card-in-chat" data-id="${product.id}">
                <div style="display: flex; align-items: center;">
                    <img src="${product.image}" alt="${product.name}">
                    <div class="product-info">
                        <strong>${product.name}</strong>
                        <p style="margin: 0; color: #dc3545; font-weight: bold;">${product.price}</p>
                    </div>
                </div>
                <div class="product-actions-chat">
                    <button class="details-btn">Ver Detalles</button>
                    <button class="add-to-cart-btn-chat" onclick="addToCartFromChat('${product.id}')">
                        Añadir al Carrito
                    </button>
                </div>
            </div>
        </div>
    `;
    chatBody.insertAdjacentHTML('beforeend', cardHtml);
}


// Scroll al fondo del chat
function scrollToBottom() {
    const chatBody = document.getElementById('chatBody');
    chatBody.scrollTop = chatBody.scrollHeight;
}

// Inicializar y conectar Quick Replies
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.quick-reply-btn').forEach(button => {
        button.addEventListener('click', (e) => {
            const action = e.target.getAttribute('data-action');
            let message = '';
            if (action === 'search') {
                message = 'Quiero buscar un producto.';
            } else if (action === 'cart') {
                message = 'Quiero ver mi carrito.';
            }

            // Simular el envío del mensaje a la IA
            document.getElementById('userInput').value = message;
            sendMessage();
        });
    });
});