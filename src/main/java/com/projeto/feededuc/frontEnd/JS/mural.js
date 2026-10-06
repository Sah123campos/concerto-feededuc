// // Carregar feedbacks relevantes para o usuário atual
// async function carregarFeedbacks() {
//     try {
//         const userType = localStorage.getItem('userType');
//         const isAdmin = localStorage.getItem('userRole') === 'ADMIN';
        
//         let url = 'http://localhost:8080/api/feedbacks';
//         if (!isAdmin && userType) {
//             // Se não for admin, filtra por categoria
//             url += `?tipoUsuario=${encodeURIComponent(userType)}`;
//         }

//         const response = await fetch(url);
//         if (!response.ok) {
//             throw new Error('Erro ao carregar feedbacks');
//         }

//         const feedbacks = await response.json();
//         exibirFeedbacks(feedbacks);
//     } catch (error) {
//         console.error('Erro:', error);
//         alert('Erro ao carregar feedbacks. Por favor, tente novamente.');
//     }
// }

// // Função para exibir os feedbacks na página
// function exibirFeedbacks(feedbacks) {
//     const container = document.getElementById('feedbacks-container');
//     container.innerHTML = ''; // Limpa o container

//     if (feedbacks.length === 0) {
//         container.innerHTML = '<p class="no-feedbacks">Nenhum feedback encontrado para sua categoria.</p>';
//         return;
//     }

//     feedbacks.forEach(feedback => {
//         const card = document.createElement('div');
//         card.className = 'feedback-card';
//         card.innerHTML = `
//             <h3>${feedback.titulo}</h3>
//             <p class="categoria">Categoria: ${feedback.categoria}</p>
//             <p class="tipo">Tipo: ${feedback.tipo}</p>
//             <p class="mensagem">${feedback.mensagem}</p>
//             <p class="data">Data: ${new Date(feedback.dataCriacao).toLocaleDateString('pt-BR')}</p>
//             ${feedback.privacidade === 'publico' ? '<span class="badge-publico">Público</span>' : '<span class="badge-privado">Privado</span>'}
//         `;
//         container.appendChild(card);
//     });
// }

// // Carregar feedbacks quando a página carregar
// document.addEventListener('DOMContentLoaded', () => {
//     const userType = localStorage.getItem('userType');
//     if (!userType) {
//         alert('Erro: Tipo de usuário não encontrado. Por favor, faça login novamente.');
//         window.location.href = 'login_cpf.html';
//         return;
//     }
//     carregarFeedbacks();
// });