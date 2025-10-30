
// ======== Referencias a los elementos del DOM ========
const listado = document.getElementById('listado-paquetes');
const itinerario = document.getElementById('itinerario-detalle');
const itinerarioTitulo = document.getElementById('itinerario-titulo');
const contenidoItinerario = document.getElementById('contenido-itinerario');
const precioPill = document.getElementById('itinerario-precio');

// ======== Mostrar itinerario ========
function mostrarItinerario(destino) {
  listado.classList.add('d-none');
  itinerario.classList.remove('d-none');
  itinerarioTitulo.innerText = `Itinerario ${destino}`;
  window.scrollTo({ top: 0, behavior: 'smooth' });

  let html = "";
  let precio = "";

  switch (destino.toLowerCase()) {
    // ---------- NACIONALES ----------
    case 'máncora':
      precio = "Por persona desde S/1,375.88 (USD 396)";
      html = `
        <h4>Día 1: Llegada y Relajación en la Playa</h4>
        <ul>
            <li><strong>Mañana:</strong> Llegada a Máncora: Arriba a Máncora por la mañana o temprano en la tarde. Puedes optar por tomar un bus o avión desde Lima o cualquier otra ciudad cercana. Si llegas por la mañana, te recomiendo hacer check-in en tu alojamiento para dejar tus cosas y empezar a disfrutar del lugar.
Desayuno en el hotel o en un restaurante local: Muchos lugares en Máncora ofrecen desayunos frescos y ricos. Prueba jugos naturales y platos con frutas tropicales.</li>
        </ul>
        <h4>Tarde</h4>
        <ul>
            <li><strong>Relájate en la playa principal de Máncora: </strong> Disfruta del sol y las aguas turquesas. Puedes caminar por la playa, tomar fotos o simplemente descansar en una hamaca.</li>
            <li><strong>Almuerzo en la playa:</strong> Hay restaurantes frente al mar que ofrecen platos frescos de mariscos, ceviches y pescados. Puedes disfrutar de una comida tranquila con vista al océano.</li>
            <li><strong>Actividades acuáticas:</strong> Si eres amante del surf o el kitesurf, este es el momento perfecto para alquilar una tabla o contratar una clase de iniciación. Si prefieres algo más tranquilo, un paseo en kayak o una tarde de snorkeling también son opciones.</li>
        </ul>
        <h4>Noche</h4>
        <ul>
<li><strong>Cena en el centro de Máncora:</strong> La oferta gastronómica es variada, con opciones de comida peruana e internacional. Hay lugares con ambiente relajado y otros más animados.</li>
            <li><strong>Vida nocturna:</strong>Después de la cena, disfruta de la vibrante vida nocturna de Máncora. Hay bares y discotecas donde puedes bailar y disfrutar de la música local o internacional. Puedes visitar lugares como "La Sirena" o "Kicks" para un ambiente más festivo.</li>       
             </ul>
        <div class="row mt-4">
          <div class="col-md-6 mb-3 mb-md-0">
              <img src="https://www.raptravelperu.com/wp-content/uploads/portada-mancora.webp"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Playa Máncora"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
          <div class="col-md-6">
              <img src="https://images.mnstatic.com/0b/65/0b65e30d7bedf13dfb35fb46476150b1.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Atardecer Máncora"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
        </div>
      `;
      break;

    case 'cusco':
      precio = "Por persona desde S/1,890.00 (USD 540)";
      html = `
        <h4>Día 1: Llegada a Cusco</h4>
        <ul><li>Traslado al hotel y aclimatación.</li></ul>
        <h4>Día 2: Valle Sagrado</h4>
        <ul><li>Visita a Pisac, Urubamba y Ollantaytambo.</li></ul>
        <h4>Día 3: Machu Picchu</h4>
        <ul><li>Excursión guiada completa.</li></ul>
        <h4>Día 4: Retorno</h4>
        <ul><li>Desayuno y traslado al aeropuerto.</li></ul>
        <div class="row mt-4">
          <div class="col-md-6 mb-3 mb-md-0">
              <img src="https://www.peru.travel/Contenido/Destino/Imagen/es/13/1.2/Principal/Cusco.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Cusco ciudad"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
          <div class="col-md-6">
              <img src="https://www.peru.travel/Contenido/Destino/Imagen/es/13/2.1/Principal/MachuPicchu.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Machu Picchu"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
        </div>
      `;
      break;

    case 'arequipa':
      precio = "Por persona desde S/950.00 (USD 270)";
      html = `
        <h4>Día 1: City Tour</h4>
        <ul><li>Monasterio de Santa Catalina y miradores.</li></ul>
        <h4>Día 2: Cañón del Colca</h4>
        <ul><li>Excursión guiada y avistamiento de cóndores.</li></ul>
        <h4>Día 3: Retorno</h4>
        <ul><li>Desayuno y traslado al aeropuerto.</li></ul>
        <div class="row mt-4">
          <div class="col-md-6 mb-3 mb-md-0">
              <img src="https://www.peru.travel/Contenido/Destino/Imagen/es/28/1.2/Principal/Arequipa.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Arequipa"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
          <div class="col-md-6">
              <img src="https://cdn.colombia.com/sdi/2023/07/25/canon-del-colca-arequipa-peru-1158162.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Cañón del Colca"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
        </div>
      `;
      break;

    case 'selva central':
      precio = "Por persona desde S/780.00 (USD 220)";
      html = `
        <h4>Día 1: Llegada a La Merced</h4>
        <ul><li>Viaje desde Lima y recepción en el hotel.</li></ul>
        <h4>Día 2: Cataratas de Bayoz</h4>
        <ul><li>Excursión y baño en las pozas naturales.</li></ul>
        <h4>Día 3: Oxapampa</h4>
        <ul><li>Tour cultural con degustación de productos locales.</li></ul>
        <h4>Día 4: Retorno</h4>
        <ul><li>Desayuno y viaje de regreso.</li></ul>
        <div class="row mt-4">
          <div class="col-md-6 mb-3 mb-md-0">
              <img src="https://www.raptravelperu.com/wp-content/uploads/2023/03/bayoz-selva-central.webp"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Cataratas de Bayoz"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
          <div class="col-md-6">
              <img src="https://www.peru.travel/Contenido/Destino/Imagen/es/108/1.1/Principal/Oxapampa.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Oxapampa"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
        </div>
      `;
      break;

    // ---------- INTERNACIONALES ----------
    case 'cancún':
      precio = "Por persona desde USD 850.00";
      html = `
        <h4>Día 1: Llegada</h4>
        <ul><li>Traslado al resort all-inclusive.</li></ul>
        <h4>Día 2: Isla Mujeres</h4>
        <ul><li>Paseo en catamarán y snorkel.</li></ul>
        <h4>Día 3: Chichén Itzá</h4>
        <ul><li>Visita guiada con almuerzo típico.</li></ul>
        <h4>Día 4: Libre</h4>
        <ul><li>Día libre en el resort.</li></ul>
        <div class="row mt-4">
          <div class="col-md-6 mb-3 mb-md-0">
              <img src="https://media.tacdn.com/media/attractions-splice-spp-674x446/07/38/69/c0.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Cancún playa"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
          <div class="col-md-6">
              <img src="https://www.mexicancaribbean.travel/wp-content/uploads/2022/03/chichen-itza.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Chichén Itzá"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
        </div>
      `;
      break;

    case 'cartagena':
      precio = "Por persona desde USD 720.00";
      html = `
        <h4>Día 1: Llegada</h4>
        <ul><li>City tour por la ciudad amurallada.</li></ul>
        <h4>Día 2: Islas del Rosario</h4>
        <ul><li>Paseo en lancha y almuerzo en playa privada.</li></ul>
        <h4>Día 3: Cultura y compras</h4>
        <ul><li>Castillo de San Felipe y Cerro de la Popa.</li></ul>
        <h4>Día 4: Libre</h4>
        <ul><li>Descanso o actividades opcionales.</li></ul>
        <div class="row mt-4">
          <div class="col-md-6 mb-3 mb-md-0">
              <img src="https://cdn.britannica.com/57/177857-050-7C13E6C9/Cartagena-Colombia.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Cartagena ciudad amurallada"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
          <div class="col-md-6">
              <img src="https://www.colombia.co/wp-content/uploads/2018/02/rosario-islands.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Islas del Rosario"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
        </div>
      `;
      break;

    case 'punta cana':
      precio = "Por persona desde USD 980.00";
      html = `
        <h4>Día 1: Llegada</h4>
        <ul><li>Traslado al resort y coctel de bienvenida.</li></ul>
        <h4>Día 2: Isla Saona</h4>
        <ul><li>Paseo en catamarán y snorkel.</li></ul>
        <h4>Día 3: Libre</h4>
        <ul><li>Relájate en la playa o spa.</li></ul>
        <h4>Día 4: Retorno</h4>
        <ul><li>Desayuno y traslado al aeropuerto.</li></ul>
        <div class="row mt-4">
          <div class="col-md-6 mb-3 mb-md-0">
              <img src="https://dynamic-media-cdn.tripadvisor.com/media/photo-o/1a/41/b0/d9/punta-cana.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Punta Cana playa"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
          <div class="col-md-6">
              <img src="https://caribejourneys.com/wp-content/uploads/2023/07/Isla-Saona-1024x683.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Isla Saona"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
        </div>
      `;
      break;

    case 'buenos aires':
      precio = "Por persona desde USD 650.00";
      html = `
        <h4>Día 1: Llegada</h4>
        <ul><li>Noche de tango y cena show.</li></ul>
        <h4>Día 2: City Tour</h4>
        <ul><li>Recoleta, Palermo y Puerto Madero.</li></ul>
        <h4>Día 3: Libre</h4>
        <ul><li>Tour opcional al Tigre o compras.</li></ul>
        <h4>Día 4: Retorno</h4>
        <ul><li>Desayuno y traslado al aeropuerto.</li></ul>
        <div class="row mt-4">
          <div class="col-md-6 mb-3 mb-md-0">
              <img src="https://cdn.britannica.com/31/121631-050-2A9E2CE5/Obelisk-Buenos-Aires-Argentina.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Obelisco Buenos Aires"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
          <div class="col-md-6">
              <img src="https://www.argentina.gob.ar/sites/default/files/styles/galleria_flexslider/public/2020-09/caminito-la-boca.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="La Boca Buenos Aires"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
        </div>
      `;
      break;

    // ---------- NUEVOS DESTINOS 🌍 ----------
    case 'dubái':
      precio = "Por persona desde USD 1,450.00";
      html = `
        <h4>Día 1: Llegada a Dubái</h4>
        <ul><li>Traslado al hotel y noche libre.</li></ul>
        <h4>Día 2: City Tour Moderno</h4>
        <ul><li>Burj Khalifa, Dubai Mall y Marina.</li></ul>
        <h4>Día 3: Safari por el desierto</h4>
        <ul><li>Cena beduina y espectáculo árabe.</li></ul>
        <h4>Día 4: Libre</h4>
        <ul><li>Excursión opcional a Abu Dhabi.</li></ul>
        <h4>Día 5: Retorno</h4>
        <ul><li>Traslado al aeropuerto.</li></ul>
        <div class="row mt-4">
          <div class="col-md-6 mb-3 mb-md-0">
              <img src="https://www.visitdubai.com/-/media/images/leisure/hero/dubai-city-landscape.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Dubái ciudad"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
          <div class="col-md-6">
              <img src="https://cdn.getyourguide.com/img/location/58c9aee7b8c76.jpeg/99.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Safari Dubái"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
        </div>
      `;
      break;

    case 'orlando':
      precio = "Por persona desde USD 1,050.00";
      html = `
        <h4>Día 1: Llegada a Orlando</h4>
        <ul><li>Traslado y descanso.</li></ul>
        <h4>Día 2: Walt Disney World</h4>
        <ul><li>Entrada a Magic Kingdom.</li></ul>
        <h4>Día 3: Universal Studios</h4>
        <ul><li>Visita a los parques Universal.</li></ul>
        <h4>Día 4: Compras</h4>
        <ul><li>Tour de shopping Premium Outlets.</li></ul>
        <h4>Día 5: Retorno</h4>
        <ul><li>Traslado al aeropuerto.</li></ul>
        <div class="row mt-4">
          <div class="col-md-6 mb-3 mb-md-0">
              <img src="https://cdn.getyourguide.com/img/location/58d1ef42f28ac.jpeg/99.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Disney Orlando"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
          <div class="col-md-6">
              <img src="https://images.unsplash.com/photo-1541339907198-e08756dedf3f"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Universal Studios Orlando"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
        </div>
      `;
      break;

    case 'osaka':
      precio = "Por persona desde USD 1,800.00";
      html = `
        <h4>Día 1: Llegada a Osaka</h4>
        <ul><li>Traslado al hotel y descanso.</li></ul>
        <h4>Día 2: Osaka y Castillo</h4>
        <ul><li>Visita guiada al Castillo de Osaka.</li></ul>
        <h4>Día 3: Kioto</h4>
        <ul><li>Excursión a templos y santuarios.</li></ul>
        <h4>Día 4: Libre</h4>
        <ul><li>Opción de visitar Nara o Universal Japan.</li></ul>
        <h4>Día 5: Retorno</h4>
        <ul><li>Traslado al aeropuerto.</li></ul>
        <div class="row mt-4">
          <div class="col-md-6 mb-3 mb-md-0">
              <img src="https://www.japan.travel/content/dam/mottainai/cities/osaka/castle.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Castillo Osaka"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
          <div class="col-md-6">
              <img src="https://www.japan.travel/content/dam/mottainai/cities/kyoto/fushimi-inari.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Kioto Fushimi Inari"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
        </div>
      `;
      break;

    case 'parís':
      precio = "Por persona desde USD 1,350.00";
      html = `
        <h4>Día 1: Llegada a París</h4>
        <ul><li>Traslado al hotel y paseo nocturno.</li></ul>
        <h4>Día 2: City Tour</h4>
        <ul><li>Torre Eiffel, Louvre y Campos Elíseos.</li></ul>
        <h4>Día 3: Versalles</h4>
        <ul><li>Excursión guiada al Palacio de Versalles.</li></ul>
        <h4>Día 4: Libre</h4>
        <ul><li>Compras o museos.</li></ul>
        <h4>Día 5: Retorno</h4>
        <ul><li>Traslado al aeropuerto.</li></ul>
        <div class="row mt-4">
          <div class="col-md-6 mb-3 mb-md-0">
              <img src="https://upload.wikimedia.org/wikipedia/commons/a/af/Tour_Eiffel_Wikimedia_Commons.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Torre Eiffel"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
          <div class="col-md-6">
              <img src="https://cdn.britannica.com/08/186908-050-898B54BA/Palace-of-Versailles-France.jpg"
                   class="img-fluid rounded-4 shadow-sm"
                   alt="Palacio de Versalles"
                   style="height:250px;object-fit:cover;width:100%;">
          </div>
        </div>
      `;
      break;

    default:
      precio = "Consulta disponibilidad";
      html = `<p>Próximamente disponible el itinerario para <strong>${destino}</strong>.</p>`;
      break;
  }

  contenidoItinerario.innerHTML = html;
  precioPill.textContent = precio;
}

// ======== Mostrar listado de paquetes ========
function mostrarListado() {
  itinerario.classList.add('d-none');
  listado.classList.remove('d-none');
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// ======== Evitar comportamiento de enlaces ========
document.querySelectorAll('.card-paquete .btn-comprar').forEach(btn => {
  btn.setAttribute('type', 'button');
});
