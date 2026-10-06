// Função para carregar feedbacks baseado no tipo de usuário
async function carregarFeedbacksFiltrados() {
    try {
        // const userType = localStorage.getItem('userType');
        // const isAdmin = localStorage.getItem('userRole') === 'ADMIN';
        
        // O mural sempre mostra apenas feedbacks públicos
        //let url = 'http://localhost:8080/api/feedbacks?privacidade=publico';

        const userType = localStorage.getItem("userType");
        const isAdmin = localStorage.getItem("userRole") === "ADMIN";

        let url = `http://localhost:8080/api/feedbacks/visiveis?funcaoUser=${encodeURIComponent(userType)}&isAdmin=${isAdmin}`;


        
        // Se não for admin, também filtra por categoria
        if (!isAdmin && userType) {
            url += `&tipoUsuario=${encodeURIComponent(userType)}`;
        }

        console.log('Carregando feedbacks públicos de:', url);
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error('Erro ao carregar feedbacks');
        }

        const feedbacks = await response.json();
        console.log('Feedbacks recebidos do servidor:', feedbacks.length);
        console.log('Dados completos dos feedbacks:', JSON.stringify(feedbacks, null, 2));
        
        if (!Array.isArray(feedbacks)) {
            console.error('Erro: Feedbacks não é um array!', feedbacks);
            mostrarErro('Erro: Formato de dados inválido recebido do servidor.');
            return;
        }
        
        exibirFeedbacks(feedbacks);
    } catch (error) {
        console.error('Erro:', error);
        mostrarErro('Erro ao carregar feedbacks. Por favor, tente novamente.');
    }
}

// Função para exibir os feedbacks no mural
function exibirFeedbacks(feedbacks) {
    console.log('exibirFeedbacks chamada com:', feedbacks.length, 'feedbacks');
    console.log('Feedbacks recebidos:', feedbacks);
    
    const container = document.getElementById('mural-container');
    if (!container) {
        console.error('Container mural-container não encontrado!');
        mostrarErro('Erro: Container do mural não encontrado.');
        return;
    }
    
    container.innerHTML = ''; // Limpa o container

    // Filtrar apenas feedbacks públicos (caso o backend não filtre)
    // Verificar tanto 'publico' quanto 'público' (com acento) e case-insensitive
    // const feedbacksPublicos = feedbacks.filter(fb => {
    //     const priv = (fb.privacidade || '').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    //     console.log('Verificando privacidade:', priv, 'do feedback:', fb.titulo);
    //     return priv === 'publico';
    // });
    
    console.log('Feedbacks públicos filtrados:', feedbacksPublicos.length);
    console.log('Feedbacks públicos:', feedbacksPublicos);
    
    if (feedbacksPublicos.length === 0) {
        console.warn('Nenhum feedback público encontrado. Total de feedbacks:', feedbacks.length);
        container.innerHTML = `
            <div class="feedback-empty">
                <p>Nenhum feedback público encontrado.</p>
                <p style="font-size: 12px; color: #999; margin-top: 10px;">
                    Total de feedbacks recebidos: ${feedbacks.length}
                </p>
            </div>`;
        return;
    }

    feedbacksPublicos.forEach((feedback, index) => {
        console.log(`Criando card ${index + 1} para:`, feedback.titulo);
        const card = document.createElement('div');
        card.className = `feedback-card ${feedback.privacidade === 'privado' ? 'feedback-card-privado' : ''}`;
        
        const title = document.createElement('div');
        title.className = 'feedback-title';
        title.textContent = feedback.titulo;

        const subtitle = document.createElement('div');
        subtitle.className = 'feedback-subtitle';
        subtitle.textContent = `${feedback.tipo} - ${feedback.categoria}`;

        const message = document.createElement('div');
        message.className = 'feedback-message';
        message.textContent = feedback.mensagem;

        const preview = document.createElement('div');
        preview.className = 'feedback-preview';
        const mensagemTexto = feedback.mensagem || '';
        preview.textContent = mensagemTexto.length > 100 ? mensagemTexto.substring(0, 100) + '...' : mensagemTexto;

        const collapseIcon = document.createElement('span');
        collapseIcon.className = 'collapse-icon';
        collapseIcon.textContent = '▼';
        collapseIcon.addEventListener('click', () => toggleMessage(card));

        card.appendChild(title);
        card.appendChild(subtitle);
        card.appendChild(preview);
        card.appendChild(message);
        card.appendChild(collapseIcon);

        container.appendChild(card);
        console.log(`Card ${index + 1} adicionado ao container`);
    });
    
    console.log(`Total de ${feedbacksPublicos.length} cards adicionados ao mural`);
    
    // Verificar se os cards foram realmente adicionados
    const cardsAdicionados = container.querySelectorAll('.feedback-card');
    console.log(`Cards encontrados no DOM: ${cardsAdicionados.length}`);
}

// Função para mostrar/ocultar a mensagem completa
function toggleMessage(card) {
    const message = card.querySelector('.feedback-message');
    const preview = card.querySelector('.feedback-preview');
    const icon = card.querySelector('.collapse-icon');

    message.classList.toggle('expanded');
    preview.style.display = message.classList.contains('expanded') ? 'none' : 'block';
    icon.classList.toggle('expanded');
}

// Função para mostrar mensagem de erro
function mostrarErro(mensagem) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.textContent = mensagem;
    document.body.appendChild(errorDiv);
    setTimeout(() => errorDiv.remove(), 5000);
}

// Verificar autenticação e carregar feedbacks quando a página carregar
document.addEventListener('DOMContentLoaded', () => {
    const userType = localStorage.getItem('userType');
    if (!userType) {
        window.location.href = 'login_cpf.html';
        return;
    }

    // Mostrar tipo de usuário no menu
    const userTypeDisplay = document.getElementById('user-type-display');
    if (userTypeDisplay) {
        userTypeDisplay.textContent = `Tipo: ${userType.charAt(0).toUpperCase() + userType.slice(1)}`;
    }

    carregarFeedbacksFiltrados();
});