document.addEventListener('DOMContentLoaded', () => {

    // ... (Lógica de Solo Ida) ...
    const toggle = document.getElementById("soloIdaToggle");
    const regresoGroup = document.getElementById("regreso-group");
    const regresoInput = document.getElementById("regreso");

    function updateRegreso() {
        if (toggle.checked) {
            regresoGroup.classList.add("d-none");
            regresoInput.disabled = true;
        } else {
            regresoGroup.classList.remove("d-none");
            regresoInput.disabled = false;
        }
    }

    if (toggle && regresoGroup && regresoInput) {
        toggle.addEventListener("change", updateRegreso);
        updateRegreso(); // corre al inicio
    }


    // ... (Lógica de Navegación) ...
    const homeContent = document.getElementById('home-content');
    const itinerarioMancora = document.getElementById('itinerario-mancora');
    const itinerarioCusco = document.getElementById('itinerario-cusco');
    
    const mostrarItinerario = (destino) => {
        if (homeContent) {
            homeContent.style.display = 'none';
        }
        
        [itinerarioMancora, itinerarioCusco].forEach(itinerario => {
            if (itinerario) itinerario.style.display = 'none';
        });

        if (destino === 'mancora' && itinerarioMancora) {
            itinerarioMancora.style.display = 'block';
        } else if (destino === 'cusco' && itinerarioCusco) {
            itinerarioCusco.style.display = 'block';
        }

        // Mover la vista al principio de la página
        window.scrollTo(0, 0); 
    };

    const volverAPaquetes = () => {
        [itinerarioMancora, itinerarioCusco].forEach(itinerario => {
            if (itinerario) itinerario.style.display = 'none';
        });

        if (homeContent) {
            homeContent.style.display = 'block'; 
        }

        // Mover la vista a la sección de paquetes (ajustamos el scroll al principio de la sección de paquetes)
        const paquetesSection = document.getElementById('paquetes-nacionales');
        window.scrollTo(0, paquetesSection ? paquetesSection.offsetTop : 0); 
    };

    // Asignar listeners a los botones "Saber más"
    document.querySelectorAll('.btn-ver-itinerario').forEach(button => {
        button.addEventListener('click', (event) => {
            event.preventDefault();
            const destino = button.getAttribute('data-destino');
            if (destino === 'mancora' || destino === 'cusco') {
                mostrarItinerario(destino);
            }
        });
    });

    // Asignar listeners a los botones "Volver"
    document.querySelectorAll('.btn-volver').forEach(button => {
        button.addEventListener('click', (event) => {
            event.preventDefault();
            volverAPaquetes();
        });
    });
});

// ============================
//  ESTADO DE LA CONVERSACIÓN
// ============================

// Objeto que almacena temporalmente los datos del viaje que el usuario está creando
// Se va llenando a medida que el usuario proporciona información
let pendingTrip = { destino: null, dias: null, categoria: null };

// Variable que controla si estamos esperando confirmación del usuario
let waitingForConfirmation = true;

// Variable que indica si estamos esperando que el usuario escriba un destino
let waitingForDestination = false;

// Guarda el último itinerario generado por la IA para poder modificarlo después
let lastItinerary = null;

// Estado actual de la conversación para saber en qué fase estamos
// "initial" = inicio, "building" = construyendo viaje, "itinerary_ready" = itinerario listo, "modifying" = modificando
let conversationState = "initial";

// ============================
//  CHATBOT UI (Interfaz visual)
// ============================

// Función que abre o cierra la ventana del chatbot al hacer clic en el botón
function toggleChatbot() {
  // Obtiene el elemento HTML donde está la ventana del chatbot
  const chatbotWindow = document.getElementById("chatbot-window");
  
  // Si la ventana está visible ("flex"), la oculta ("none"), y viceversa
  chatbotWindow.style.display =
    chatbotWindow.style.display === "flex" ? "none" : "flex";
}

// Función que agrega un mensaje al chat (puede ser del usuario o del bot)
function addMessage(sender, text, isButtons = false) {
  // Obtiene el contenedor donde se muestran todos los mensajes
  const messagesDiv = document.getElementById("chatbot-messages");

  // Crea un nuevo elemento div para este mensaje
  const messageDiv = document.createElement("div");
  
  // Añade la clase CSS correspondiente según quien envíe el mensaje
  // Si es "user" añade "user-message", si no, añade "bot-message"
  messageDiv.classList.add(sender === "user" ? "user-message" : "bot-message");

  // Si el mensaje contiene botones HTML, usa innerHTML para renderizarlos
  if (isButtons) {
    messageDiv.innerHTML = text;
  } else {
    // Si es texto normal, usa textContent por seguridad (evita inyección de código)
    messageDiv.textContent = text;
  }

  // Añade el mensaje nuevo al contenedor
  messagesDiv.appendChild(messageDiv);
  
  // Hace scroll automático hacia abajo para ver siempre el último mensaje
  messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

// ============================
//  SUGERENCIAS Y OPCIONES
// ============================

// Muestra una lista de destinos sugeridos con botones clicables
function showDestinationSuggestions() {
  // Array con destinos populares predefinidos
  const destinos = ["París", "Japón", "México", "Nueva York", "Cusco"];
  
  // Crea botones HTML para cada destino usando map y join
  // map() recorre cada destino y crea un string de botón
  // join(" ") une todos los botones con un espacio entre ellos
  let buttons = destinos
    .map(
      (dest) =>
        `<button class="option-btn" onclick="selectDestination('${dest}')">${dest}</button>`
    )
    .join(" ");
    
  // Muestra los botones como un mensaje del bot (con HTML)
  addMessage("bot", "🌍 Te recomiendo algunos destinos: <br>" + buttons, true);
}

// Se ejecuta cuando el usuario hace clic en uno de los botones de destino
function selectDestination(destino) {
  // Guarda el destino seleccionado en nuestro objeto pendingTrip
  pendingTrip.destino = destino;
  
  // Cambia el estado de la conversación a "construyendo"
  conversationState = "building";
  
  // Muestra el destino como un mensaje del usuario
  addMessage("user", destino);

  // Si aún no tenemos los días de viaje, preguntamos por ellos
  if (!pendingTrip.dias) {
    addMessage("bot", "📅 ¿Cuántos días quieres viajar?");
  } 
  // Si no tenemos la categoría, preguntamos por ella
  else if (!pendingTrip.categoria) {
    askCategoria();
  } 
  // Si ya tenemos todo, generamos la consulta final
  else {
    buildFinalQuery();
  }
}

// Muestra botones para que el usuario seleccione la categoría del viaje
function askCategoria() {
  // Crea tres botones HTML para las categorías de presupuesto
  let buttons = `
    <button class="option-btn" onclick="selectCategoria('bajo')">Bajo</button>
    <button class="option-btn" onclick="selectCategoria('medio')">Medio</button>
    <button class="option-btn" onclick="selectCategoria('alto')">Alto</button>
  `;
  
  // Muestra el mensaje preguntando por la categoría
  addMessage("bot", "💰 ¿Qué categoría prefieres?", true);
  
  // Muestra los botones de categoría
  addMessage("bot", buttons, true);
}

// Se ejecuta cuando el usuario selecciona una categoría
function selectCategoria(cat) {
  // Guarda la categoría seleccionada en pendingTrip
  pendingTrip.categoria = cat;
  
  // Muestra la categoría como mensaje del usuario
  addMessage("user", cat);
  
  // Ya tenemos toda la información, generamos la consulta completa
  buildFinalQuery();
}

// ============================
//  RENDER RESPUESTA IA (Renderiza la respuesta de la Inteligencia Artificial)
// ============================

// Función que toma la respuesta de la IA y la muestra bonita en el chat
function renderIAResponse(respuestaIA, isModification = false) {
  // Obtiene el contenedor de mensajes
  const messagesDiv = document.getElementById("chatbot-messages");
  
  // Crea un contenedor nuevo para toda la respuesta
  const container = document.createElement("div");
  container.classList.add("bot-message");

  try {
    // Busca dónde empieza el JSON en la respuesta de la IA
    const jsonStart = respuestaIA.indexOf("{");
    
    // Si no encuentra JSON, lanza un error
    if (jsonStart === -1) throw new Error("No JSON found");

    // Extrae solo la parte JSON de la respuesta
    const jsonText = respuestaIA.substring(jsonStart);
    
    // Convierte el texto JSON en un objeto JavaScript que podemos usar
    const data = JSON.parse(jsonText);

    // VALIDACIÓN CRÍTICA: Si es una modificación, verifica que el destino no haya cambiado
    if (isModification && lastItinerary) {
      // Obtiene el destino anterior y el nuevo, en minúsculas y sin espacios extras
      const destinoAnterior = lastItinerary.destino.toLowerCase().trim();
      const destinoNuevo = data.destino.toLowerCase().trim();

      // Si los destinos son diferentes (y no están contenidos uno en otro), es un error
      if (destinoAnterior !== destinoNuevo && !destinoNuevo.includes(destinoAnterior) && !destinoAnterior.includes(destinoNuevo)) {
        // Muestra mensaje de error
        addMessage("bot", `⚠️ Error: La IA cambió el destino de ${lastItinerary.destino} a ${data.destino}. Esto no debería pasar.`);
        addMessage("bot", "Por favor, intenta reformular tu solicitud o escríbeme de nuevo qué deseas cambiar.");
        
        // Regresa al estado de itinerario listo
        conversationState = "itinerary_ready";
        return; // Sale de la función
      }

      // Fuerza que el destino sea el original (por seguridad)
      data.destino = lastItinerary.destino;
    }

    // Guarda este itinerario como el último para futuras modificaciones
    lastItinerary = data;
    
    // Cambia el estado a "itinerario listo"
    conversationState = "itinerary_ready";

    // === Crea la cabecera general del itinerario ===
    let header = `
      <div class="plan-card">
        <h5>🌍 Destino: ${data.destino}</h5>
        <p><b>Duración:</b> ${data.duracion || "No especificada"}</p>
        <p><b>Categoría:</b> ${data.categoria || data.presupuesto || "General"}</p>
      </div>
    `;
    container.innerHTML += header;

    // === Recorre y muestra cada día del plan de viaje ===
    if (data.planes && Array.isArray(data.planes)) {
      // Para cada plan diario en el array
      data.planes.forEach((plan) => {
        // Crea una tarjeta HTML con la información del día
        let card = `
          <div class="plan-card">
            <h6>📅 Día ${plan.dia}</h6>
            <p><b>Actividades:</b> ${Array.isArray(plan.actividades) ? plan.actividades.join(", ") : plan.actividades}</p>
            <p><b>Alojamiento:</b> ${plan.alojamiento || "No especificado"}</p>
            <p><b>Comida:</b> ${Array.isArray(plan.comida) ? plan.comida.join(", ") : plan.comida || "No especificada"}</p>
            <p><b>Transporte:</b> ${plan.transporte || "No especificado"}</p>
            <p><b>Costo estimado:</b> 
              ${plan.costoEstimado?.monedaDestino || ""} 
              (${plan.costoEstimado?.USD || ""} / ${plan.costoEstimado?.PEN || ""})
            </p>
          </div>
        `;
        // Añade la tarjeta al contenedor
        container.innerHTML += card;
      });
    }

    // === Muestra el costo total del viaje ===
    if (data.totalCosto) {
      let total = `
        <div class="plan-card total">
          <b>Total estimado:</b> ${data.totalCosto.monedaDestino || ""} 
          (${data.totalCosto.USD || ""} / ${data.totalCosto.PEN || ""})
        </div>
      `;
      container.innerHTML += total;
    }

    // Muestra mensaje diferente según si es modificación o itinerario nuevo
    if (isModification) {
      addMessage("bot", "✅ He actualizado tu itinerario con los cambios solicitados. 😊");
    } else {
      addMessage("bot", "✅ ¡Aquí está tu itinerario! 😊");
    }

    // Muestra opciones de qué hacer después (modificar o nuevo viaje)
    let actionButtons = `
  <div class="bot-followup" style="margin-top: 10px;">
    <p>✨ Si quieres hacer algún cambio en tu viaje, solo dímelo.  </p>
    <p>🌍 También puedo ayudarte a planear otro viaje cuando quieras.</p>
    <div style="margin-top: 8px;">
      <button class="option-btn" onclick="startNewTrip()" style="background: #28a745;">🆕 Empezar otro viaje</button>
    </div>
  </div>
`;
    addMessage("bot", actionButtons, true);

  } catch (e) {
    // Si hay error al procesar el JSON, muestra la respuesta tal cual
    container.textContent = respuestaIA;
    conversationState = "itinerary_ready";
  }

  // Añade el contenedor completo al chat
  messagesDiv.appendChild(container);
  
  // Hace scroll hacia abajo para ver el itinerario completo
  messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

// ============================
//  DETECTAR SI ES UNA MODIFICACIÓN O NUEVO VIAJE
// ============================

// Función que detecta si el usuario quiere hacer un NUEVO viaje
function isNewTripRequest(message) {
  // Array con patrones (expresiones regulares) que indican un nuevo viaje
  const newTripPatterns = [
    /(?:nuevo|otra|diferente)\s+(?:viaje|itinerario|plan)/i, // "nuevo viaje", "otro plan"
    /(?:quiero|voy a|planeo|pienso)\s+(?:viajar|ir)\s+a\s+([a-zA-Záéíóúñ\s]+)/i, // "quiero viajar a París"
    /(?:cambiar|modificar)\s+(?:el\s+)?destino\s+a\s+([a-zA-Záéíóúñ\s]+)/i, // "cambiar destino a Roma"
    /(?:ahora|mejor)\s+(?:quiero ir|voy)\s+a\s+([a-zA-Záéíóúñ\s]+)/i, // "ahora quiero ir a España"
    /(?:plan|itinerario)\s+(?:para|a)\s+([a-zA-Záéíóúñ\s]+)/i, // "plan para México"
    /^([a-zA-Záéíóúñ\s]+)\s+por\s+\d+\s+d[ií]as?/i // "España por 5 días"
  ];

  // Verifica si algún patrón coincide con el mensaje
  // some() devuelve true si al menos uno coincide
  return newTripPatterns.some(pattern => pattern.test(message));
}

// Función que detecta si el usuario quiere MODIFICAR el itinerario actual
function isModificationRequest(message) {
  // Si es una solicitud de nuevo viaje, NO es modificación
  if (isNewTripRequest(message)) {
    return false;
  }

  // Palabras clave que indican modificación (sin cambiar destino)
  const modificacionKeywords = [
    "vegetariano", "vegetariana", "vegano", "vegana", "sin carne",
    "económico", "barato", "lujo", "premium", "caro",
    "sin gluten", "kosher", "halal", "orgánico", "local",
    "más", "menos", "otro hotel", "otra actividad", "otros restaurantes"
  ];

  // Patrones específicos que indican modificación
  const patterns = [
    /(?:comida|comer|cenar|almorzar|restaurante|platos?)\s+(?:vegetarian[oa]|vegan[oa]|sin\s+carne|sin\s+gluten)/i,
    /hotel(?:es)?\s+(?:más|menos|otro|otra|diferente)\s+(?:económico|barato|lujoso|premium|caro)/i,
    /actividad(?:es)?\s+(?:más|menos|otra|diferentes)\s+(?:cultural(?:es)?|aventura|relajante)/i,
    /(?:quiero|prefiero|me gustaría)\s+(?:otro|otra|diferente|más|menos)\s+(?:hotel|restaurante|actividad)/i,
    /sin\s+(?:carne|gluten|lactosa|mariscos|pescado)/i,
    /(?:incluye|agrega|añade|pon|dame)\s+(?:más|menos)/i,
    /(?:mejor|peor)\s+(?:hotel|alojamiento|comida|restaurante)(?!\s+en\s+[a-zA-Z])/i,
    /opciones?\s+(?:vegetarian[oa]|vegan[oa]|económic[oa]|organic[oa])/i
  ];

  // Convierte el mensaje a minúsculas para comparar
  const messageLower = message.toLowerCase();

  // Si menciona "destino", probablemente quiere cambiar de destino (no modificar)
  if (messageLower.includes("destino")) {
    return false;
  }

  // Verifica si el mensaje contiene alguna palabra clave de modificación
  const hasModificationKeyword = modificacionKeywords.some(keyword =>
    messageLower.includes(keyword)
  );

  // Verifica si el mensaje coincide con algún patrón de modificación
  const matchesPattern = patterns.some(pattern => pattern.test(message));

  // Devuelve true si cumple con alguna de las dos condiciones
  return hasModificationKeyword || matchesPattern;
}

// ============================
//  CONSULTAS AL BACKEND (Servidor)
// ============================

// Construye la consulta final con todos los datos y la envía al servidor
function buildFinalQuery() {
  // Inicia la consulta con "Plan de viaje"
  let consulta = "Plan de viaje";

  // Añade el destino si existe
  if (pendingTrip.destino) consulta += ` a ${pendingTrip.destino}`;
  
  // Añade los días si existen
  if (pendingTrip.dias) consulta += ` por ${pendingTrip.dias} días`;
  
  // Añade la categoría si existe
  if (pendingTrip.categoria) consulta += ` en categoría ${pendingTrip.categoria}`;

  // Si no hay destino, no podemos continuar
  if (!pendingTrip.destino) {
    addMessage("bot", "Por favor indícame el destino antes de continuar. 🌍");
    return;
  }

  // Cambia el estado a "construyendo"
  conversationState = "building";
  
  // Envía la consulta al backend (false indica que NO es modificación)
  askBackend(consulta.trim(), false);
}

// Función que envía la consulta al servidor y recibe la respuesta de la IA
function askBackend(consulta, isModification = false) {
  // URL del servidor donde está el backend (ajusta si tu puerto es diferente)
  const urlBase = `http://localhost:8080/chatbot/preguntar?usuarioId=1`;

  // === CASO 1: Es una MODIFICACIÓN de un itinerario existente ===
  if (isModification && lastItinerary) {
    // Obtiene el destino actual del itinerario
    let destinoActual = lastItinerary.destino || "tu destino";
    
    // Detecta qué tipo de cambio está pidiendo el usuario
    let detalleCambio = detectarTipoCambio(consulta, destinoActual);

    // Muestra un mensaje indicando qué se va a modificar
    addMessage("bot", detalleCambio);

    // Prepara el cuerpo de la petición con todos los datos necesarios
    const body = {
      mensaje: consulta, // El mensaje del usuario
      esModificacion: true, // Indica que es una modificación
      itinerarioPrevio: lastItinerary, // El itinerario anterior completo
      instrucciones: `REGLAS ADICIONALES:
- Si el usuario menciona "vegetariana", "vegana" o "sin carne", asegúrate de que todos los platos de comida sean vegetarianos/veganos
- Si menciona "económico" o "barato", reduce los costos y elige opciones más accesibles
- Si menciona "lujo" o "premium", incrementa la calidad y los costos
- Mantén siempre el mismo destino: ${destinoActual}`
    };

    // Envía la petición al servidor usando fetch (AJAX moderno)
    fetch(urlBase, {
      method: "POST", // Método HTTP POST
      headers: { "Content-Type": "application/json" }, // Indica que enviamos JSON
      body: JSON.stringify(body) // Convierte el objeto a texto JSON
    })
      .then((response) => response.json()) // Convierte la respuesta a objeto JavaScript
      .then((data) => {
        // Muestra en consola la respuesta del servidor (para debugging)
        console.log("Respuesta modificación backend:", data);
        
        // Si hay respuesta de la IA, la renderiza
        if (data.respuestaIA) {
          renderIAResponse(data.respuestaIA, true);
        } else if (data.respuesta) {
          // Por si el backend usa otro nombre de campo
          renderIAResponse(data.respuesta, true);
        } else {
          // Si no hay respuesta válida, muestra error
          addMessage("bot", "⚠️ Hubo un problema al procesar tu consulta (modificación).");
        }
        
        // Regresa al estado de itinerario listo
        conversationState = "itinerary_ready";
      })
      .catch((err) => {
        // Si hay error de conexión, lo muestra en consola y al usuario
        console.error("Error modificación:", err);
        addMessage("bot", "❌ Error al conectar con el servidor.");
        conversationState = "itinerary_ready";
      });

  } else {
    // === CASO 2: Es un NUEVO ITINERARIO ===
    
    // Prepara el cuerpo de la petición para nuevo itinerario
    const body = {
      mensaje: consulta, // La consulta completa del usuario
      esModificacion: false // Indica que NO es modificación
    };

    // Envía la petición al servidor
    fetch(urlBase, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body)
    })
      .then((response) => response.json())
      .then((data) => {
        // Muestra la respuesta en consola
        console.log("Respuesta nuevo itinerario backend:", data);
        
        // Si hay respuesta de la IA, la renderiza
        if (data.respuestaIA) {
          renderIAResponse(data.respuestaIA, false);
          // Reinicia pendingTrip después de recibir respuesta exitosa
          pendingTrip = { destino: null, dias: null, categoria: null };
        } else if (data.respuesta) {
          // Por si el backend usa otro nombre
          renderIAResponse(data.respuesta, false);
          pendingTrip = { destino: null, dias: null, categoria: null };
        } else {
          // Si no hay respuesta válida
          addMessage("bot", "⚠️ Hubo un problema al procesar tu consulta (nuevo itinerario).");
        }
        
        conversationState = "itinerary_ready";
      })
      .catch((err) => {
        // Manejo de errores
        console.error("Error nuevo itinerario:", err);
        addMessage("bot", "❌ Error al conectar con el servidor.");
        conversationState = "itinerary_ready";
      });
  }
}

// Detecta qué tipo de cambio está solicitando el usuario y devuelve un mensaje apropiado
function detectarTipoCambio(consulta, destino) {
  // Busca palabras relacionadas con comida/dieta
  if (/vegetarian|vegano|sin carne|sin gluten/i.test(consulta)) {
    return `🍴 Entendido, voy a actualizar las opciones de comida en tu itinerario de ${destino}...`;
  } 
  // Busca palabras relacionadas con alojamiento
  else if (/hotel|alojamiento|hospedaje/i.test(consulta)) {
    return `🏨 Perfecto, voy a actualizar tu alojamiento en ${destino}...`;
  } 
  // Busca palabras relacionadas con actividades
  else if (/actividad|paseo|excursión|tour|visita/i.test(consulta)) {
    return `🎡 De acuerdo, voy a modificar las actividades de tu itinerario en ${destino}...`;
  } 
  // Busca palabras relacionadas con restaurantes
  else if (/restaurante|comida|cenar|almorzar/i.test(consulta)) {
    return `🍽️ Entendido, voy a cambiar los restaurantes de tu itinerario en ${destino}...`;
  } 
  // Mensaje genérico si no encaja en ninguna categoría
  else {
    return `✨ Voy a actualizar tu itinerario de ${destino} según tu solicitud...`;
  }
}

// ============================
//  MANEJO DE MENSAJES DEL USUARIO
// ============================

// Función que capitaliza la primera letra de cada palabra en un texto
function capitalizarDestino(texto) {
  return texto
    .split(" ") // Separa el texto en palabras
    .map(p => p.charAt(0).toUpperCase() + p.slice(1).toLowerCase()) // Capitaliza cada palabra
    .join(" "); // Une las palabras de nuevo
}

// Función principal que procesa y envía el mensaje del usuario
function sendMessage() {
  // Obtiene el campo de entrada del usuario
  const input = document.getElementById("user-input");
  
  // Obtiene el texto y elimina espacios al inicio/final
  const message = input.value.trim();

  // Si el mensaje está vacío, no hace nada
  if (!message) return;

  // Muestra el mensaje del usuario en el chat
  addMessage("user", message);
  
  // Limpia el campo de entrada
  input.value = "";

  // === CASO 1: DETECTAR NUEVO VIAJE (incluso si hay itinerario previo) ===
  if (isNewTripRequest(message)) {
    // Limpia el itinerario anterior
    lastItinerary = null;
    conversationState = "building";

    // Intenta extraer destino, días y categoría del mensaje usando regex
    let matchNuevoViaje = message.match(
      /(?:quiero ir a|viajar a|viaje a|plan para|itinerario para|destino a|ir a)\s+([a-zA-Záéíóúñ\s]+)(?:\s+por\s+(\d+)\s*d[ií]as?)?(?:\s+en\s+(?:categor[ií]a|categoria)\s+(\w+))?/i
    );

    if (matchNuevoViaje) {
      // Extrae y guarda el destino
      let destinoDetectado = matchNuevoViaje[1].trim();
      pendingTrip.destino = capitalizarDestino(destinoDetectado);
      
      // Extrae días si existen
      pendingTrip.dias = matchNuevoViaje[2] ? matchNuevoViaje[2].trim() : null;
      
      // Extrae categoría si existe
      pendingTrip.categoria = matchNuevoViaje[3] ? matchNuevoViaje[3].toLowerCase() : null;

      addMessage("bot", `✈️ Perfecto 😄 Planearemos un viaje a ${pendingTrip.destino}.`);

      // Si faltan los días, los pide
      if (!pendingTrip.dias) {
        addMessage("bot", "📅 ¿Cuántos días quieres viajar?");
        return;
      }

      // Si falta la categoría, la pide
      if (!pendingTrip.categoria) {
        askCategoria();
      } 
      // Si tiene todo, genera la consulta
      else {
        buildFinalQuery();
      }
      return;
    } else {
      // No se pudo extraer el destino del mensaje
      addMessage("bot", "✈️ ¿A qué destino quieres viajar?");
      waitingForDestination = true;
      return;
    }
  }

  // === CASO 2: YA HAY UN ITINERARIO Y EL USUARIO QUIERE HACER UNA MODIFICACIÓN ===
if (conversationState === "itinerary_ready" && lastItinerary && isModificationRequest(message)) {
  // Cambiamos el estado a "modificando" para que el backend sepa que no es un nuevo viaje
  conversationState = "modifying";

  // 🔍 Verificamos si el usuario menciona un cambio concreto (como "cambiar el restaurante del segundo día")
  const matchCambio = message.match(/(cambiar|modificar|editar)\s+(?:el|la)?\s*(.*?)(?:\s+del\s+(\d+)[°º]?\s*d[ií]a)?/i);

  if (matchCambio) {
    const elemento = matchCambio[2]?.trim();  // lo que se quiere cambiar (ej: restaurante, hotel, actividad)
    const dia = matchCambio[3];               // el día si lo menciona (ej: 2)

    // 💬 Confirmamos el cambio que entendimos
    if (dia) {
      addMessage("bot", `🔄 Entendido, vas a cambiar el ${elemento} del día ${dia}.`);
    } else {
      addMessage("bot", `🔄 Perfecto, haré un ajuste en ${elemento}.`);
    }
  } else {
    // Si no detecta una estructura clara, avisa que procesará la modificación
    addMessage("bot", "🔧 Entendido, haré la modificación en tu itinerario.");
  }

  // 🚀 Enviamos el mensaje al backend para procesar la modificación
  askBackend(message, true);
  return;
}


  // === CASO 3: CONSTRUYENDO ITINERARIO (esperando datos del usuario) ===
  if (conversationState === "building" || waitingForDestination) {
    
    // Si solo falta el destino
    if (waitingForDestination) {
      pendingTrip.destino = message;
      waitingForDestination = false;

      // Pregunta por los días si no los tiene
      if (!pendingTrip.dias) {
        addMessage("bot", "📅 ¿Cuántos días quieres viajar?");
        return;
      } 
      // Pregunta por categoría si no la tiene
      else if (!pendingTrip.categoria) {
        askCategoria();
        return;
      } 
      // Si tiene todo, genera consulta
      else {
        buildFinalQuery();
        return;
      }
    }

    // Si solo faltan los días
    if (pendingTrip.destino && !pendingTrip.dias) {
      // Busca un número en el mensaje
      let dias = message.match(/\d+/);
      
      if (dias) {
        // Guarda los días
        pendingTrip.dias = dias[0];
        
        // Si falta categoría, la pide
        if (!pendingTrip.categoria) {
          askCategoria();
        } 
        // Si tiene todo, genera consulta
        else {
          buildFinalQuery();
        }
      } else {
        // Si no encuentra número, pide que lo escriba
        addMessage("bot", "📅 Por favor, indícame cuántos días (un número).");
      }
      return;
    }

    // Si solo falta la categoría
    if (pendingTrip.destino && pendingTrip.dias && !pendingTrip.categoria) {
      // Verifica si el mensaje contiene una categoría válida
      if (/bajo|medio|alto/i.test(message)) {
        pendingTrip.categoria = message.toLowerCase();
        buildFinalQuery();
      } else {
        // Si no es válida, muestra los botones de nuevo
        askCategoria();
      }
      return;
    }
  }

  // === CASO 4: PRIMER MENSAJE (estado inicial) ===
  if (conversationState === "initial") {
    conversationState = "building";

    // Si el usuario dice "sí"
    if (/sí|si/i.test(message)) {
      addMessage("bot", "✈️ Perfecto, ¿a qué destino quieres viajar?");
      waitingForDestination = true;
    } 
    // Si el usuario dice "no"
    else if (/no/i.test(message)) {
      // Muestra sugerencias de destinos
      showDestinationSuggestions();
    } 
    // Si no es sí ni no, asume que escribió un destino directamente
    else {
      pendingTrip.destino = message;
      addMessage("bot", "📅 ¿Cuántos días quieres viajar?");
    }
    return;
  }

    // === CASO ESPECIAL: EL USUARIO DA LAS GRACIAS ===
  if (/gracias|muchas gracias|mil gracias|te agradezco/i.test(message)) {
    addMessage("bot", "😊 ¡De nada! Me alegra poder ayudarte.");
    
    if (conversationState === "itinerary_ready") {
      addMessage("bot", "✈️ Si quieres modificar algo del itinerario o planear otro viaje, solo dime.");
    } else {
      addMessage("bot", "💬 Si necesitas ayuda con tu viaje, estoy aquí.");
    }
    return;
  }


  // === CASO 5: TIENE ITINERARIO PERO EL MENSAJE NO ES CLARO ===
  if (conversationState === "itinerary_ready" && lastItinerary) {
    // Le recuerda al usuario que tiene un itinerario y qué puede hacer
    addMessage("bot", `💡 Tienes un itinerario para ${lastItinerary.destino}.`);
    addMessage("bot", "Puedes pedirme:\n• Modificaciones (ej: 'comida vegetariana', 'hoteles más baratos')\n• Un nuevo viaje (ej: 'quiero viajar a París')");
    return;
  }

  // === CASO 6: FALLBACK (no se entiende el mensaje) ===
  addMessage("bot", "🤔 No estoy seguro de entender. ¿Puedes ser más específico?");
  addMessage("bot", "💬 Puedes decirme: 'Quiero viajar a [destino] por [X] días'");
}

// ============================
//  EVENTO: ENTER PARA ENVIAR MENSAJE
// ============================

// Espera a que la página cargue completamente
document.addEventListener("DOMContentLoaded", () => {
  // Obtiene el campo de entrada del usuario
  const input = document.getElementById("user-input");
  
  if (input) {
    // Añade un evento que escucha cuando se presiona una tecla
    input.addEventListener("keypress", function (e) {
      // Si la tecla presionada es Enter, envía el mensaje
      if (e.key === "Enter") sendMessage();
    });
  }
});

// Se ejecuta cuando la ventana termina de cargar completamente
window.addEventListener("load", () => {
  // Obtiene el contenedor de mensajes del chat
  const chatContainer = document.getElementById("chatbot-messages");
  
  if (chatContainer) {
    // Crea el mensaje inicial del bot (saludo)
    const initialMessage = document.createElement("div");
    initialMessage.classList.add("bot-message");
    initialMessage.innerText =
      "👋 ¡Hola! Soy tu asistente de viajes. ¿A qué destino quieres viajar?";
    
    // Añade el mensaje de saludo al chat
    chatContainer.appendChild(initialMessage);
  }
});

// ============================
//  FUNCIONES DE AYUDA
// ============================

// Función que reinicia todo para empezar un nuevo viaje
function startNewTrip() {
  // Borra el itinerario anterior
  lastItinerary = null;
  
  // Cambia el estado a "construyendo"
  conversationState = "building";
  
  // Reinicia el objeto de viaje pendiente
  pendingTrip = { destino: null, dias: null, categoria: null };
  
  // Activa la bandera de espera de destino
  waitingForDestination = true;

  // Mensajes de confirmación al usuario
  addMessage("bot", "🆕 ¡Perfecto! Empecemos un nuevo viaje.");
  addMessage("bot", "✈️ ¿A qué destino quieres viajar?");
}

// Función que muestra ayuda sobre cómo modificar el itinerario
function showModificationHelp() {
  // Verifica si existe un itinerario creado
  if (!lastItinerary) {
    addMessage("bot", "✈️ Aún no tienes un itinerario creado. Primero planifiquemos uno, ¿te parece?");
    return;
  }

  // Obtiene el destino del itinerario actual
  const destino = lastItinerary.destino || "tu destino";

  // Crea un mensaje explicativo con ejemplos de modificaciones
  const mensajeNatural = `
    😊 Claro, puedo ayudarte a ajustar tu itinerario de ${destino}.
    Solo dime qué quieres cambiar: puedo modificar actividades, comidas, hoteles o presupuesto.
    <br><br>Por ejemplo:
    <ul style="text-align:left; margin:6px 0 0 15px;">
      <li>"Quiero algo más económico"</li>
      <li>"Agrega actividades culturales"</li>
      <li>"Hazlo más lujoso"</li>
      <li>"Comidas vegetarianas"</li>
    </ul>
    <br>Estoy aquí para que tu viaje quede justo como lo imaginas. 🌍
  `;

  // Muestra el mensaje de ayuda (con HTML para la lista)
  addMessage("bot", mensajeNatural, true);
}