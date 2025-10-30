// Lógica del temporizador de Ventas Flash
function updateTimer() {
    // Establece la fecha final para la cuenta regresiva (ejemplo: 2 días y 3 horas desde ahora)
    const endDate = new Date();
    endDate.setDate(endDate.getDate() + 2);
    endDate.setHours(endDate.getHours() + 3);

    // Si la fecha objetivo está en el pasado, reinicia o detiene
    // Para fines de la demo, simplemente usamos un tiempo fijo como el de la imagen
    const targetTime = 86400 * 3 + 3600 * 23 + 60 * 19 + 56; // Ejemplo: 3 días, 23 horas, 19 min, 56 seg
    let remainingTime = targetTime;

    setInterval(() => {
        if (remainingTime <= 0) {
            remainingTime = 0; // O reiniciar el temporizador si es una venta recurrente
        } else {
            remainingTime--;
        }

        const days = Math.floor(remainingTime / (3600 * 24));
        const hours = Math.floor((remainingTime % (3600 * 24)) / 3600);
        const minutes = Math.floor((remainingTime % 3600) / 60);
        const seconds = Math.floor(remainingTime % 60);

        document.getElementById('days').textContent = String(days).padStart(2, '0');
        document.getElementById('hours').textContent = String(hours).padStart(2, '0');
        document.getElementById('minutes').textContent = String(minutes).padStart(2, '0');
        document.getElementById('seconds').textContent = String(seconds).padStart(2, '0');
    }, 1000);
}

        document.addEventListener('DOMContentLoaded', updateTimer);

    /**
     * Inicializa o recupera el carrito de compras desde localStorage.
     */
    function getCart() {
        const cartJSON = localStorage.getItem('techzone_cart');
        return cartJSON ? JSON.parse(cartJSON) : [];
    }

    /**
     * Guarda el array del carrito en localStorage.
     */
    function saveCart(cart) {
        localStorage.setItem('techzone_cart', JSON.stringify(cart));
        // Opcional: Podrías llamar a una función aquí para actualizar el pequeño contador del carrito en el navbar.
    }

    /**
     * Función principal para añadir un producto al carrito (desde el botón).
     */
    function addToCart(event) {
        // 1. Obtener la tarjeta del producto y sus datos
        const productCard = event.target.closest('.product-card');
        const id = productCard.getAttribute('data-id');
        const name = productCard.getAttribute('data-name');
        const price = parseFloat(productCard.getAttribute('data-price'));
        const image = productCard.getAttribute('data-image');

        if (!id || isNaN(price)) {
            console.error('Faltan datos esenciales del producto para añadirlo al carrito.');
            return;
        }

        // 2. Obtener el carrito actual
        const cart = getCart();

        // 3. Verificar si el producto ya existe
        const existingItem = cart.find(item => item.id === id);

        if (existingItem) {
            // Si existe, incrementa la cantidad
            existingItem.quantity += 1;
        } else {
            // Si no existe, lo añade con cantidad 1
            cart.push({
                id: id,
                name: name,
                price: price,
                image: image,
                quantity: 1
            });
        }

        // 4. Guardar el carrito actualizado en localStorage
        saveCart(cart);

        // Feedback al usuario
        alert(`¡${name} añadido al carrito!`);
    }

