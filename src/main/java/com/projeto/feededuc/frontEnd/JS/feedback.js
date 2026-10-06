var tipoSelecionado = null;

document.addEventListener('DOMContentLoaded', function() {

    const mainOptions = document.querySelectorAll('.main-option');
    
    mainOptions.forEach(option => {
        option.addEventListener('click', function() {
          
            document.querySelectorAll('.sub-options').forEach(menu => {
                if (menu.id !== this.getAttribute('data-target')) {
                    menu.classList.remove('active');
                    menu.previousElementSibling.classList.remove('active');
                }
            });
            
           
            const targetId = this.getAttribute('data-target');
            const subMenu = document.getElementById(targetId);
            this.classList.toggle('active');
            subMenu.classList.toggle('active');
        });
    });

    
    const subOptions = document.querySelectorAll('.sub-option');
    
    subOptions.forEach(option => {
        option.addEventListener('click', function() {
            const mainOption = this.closest('.sub-options').previousElementSibling;
            mainOption.textContent = this.textContent + ' ';
            this.closest('.sub-options').classList.remove('active');
            mainOption.classList.remove('active');
        });
    });

    
    const privacyScroller = document.querySelector('.privacy-scroller');
    const privacyOptions = document.querySelectorAll('.privacy-option');
    
    privacyOptions.forEach(option => {
        option.addEventListener('click', function() {
            const input = this.querySelector('input');
            input.checked = true;
            
            // Atualizar descrições de privacidade
            const privadoDesc = document.getElementById('privado-desc');
            const publicoDesc = document.getElementById('publico-desc');
            
            if (input.id === 'privado') {
                privadoDesc.style.display = 'block';
                publicoDesc.style.display = 'none';
            } else {
                privadoDesc.style.display = 'none';
                publicoDesc.style.display = 'block';
            }
           
            this.scrollIntoView({
                behavior: 'smooth',
                block: 'nearest',
                inline: 'center'
            });
        });
    });
    
    // Atualizar descrições quando os inputs mudam (por teclado ou outro método)
    const privadoInput = document.getElementById('privado');
    const publicoInput = document.getElementById('publico');
    const privadoDesc = document.getElementById('privado-desc');
    const publicoDesc = document.getElementById('publico-desc');
    
    privadoInput.addEventListener('change', function() {
        if (this.checked) {
            privadoDesc.style.display = 'block';
            publicoDesc.style.display = 'none';
        }
    });
    
    publicoInput.addEventListener('change', function() {
        if (this.checked) {
            privadoDesc.style.display = 'none';
            publicoDesc.style.display = 'block';
        }
    });

    // Seleção do tipo de feedback
    const tipoOptions = document.querySelectorAll('#type-options .sub-option');
    tipoOptions.forEach(option => {
        option.addEventListener('click', function() {
            tipoOptions.forEach(o => o.classList.remove('selected'));
            this.classList.add('selected');
            window.tipoSelecionado = this.textContent.trim().toLowerCase();
        });
    });

    const categoryOptions = document.querySelectorAll('#category-options .sub-option');
    categoryOptions.forEach(option => {
        option.addEventListener('click', function() {
            categoryOptions.forEach(o => o.classList.remove('selected'));
            this.classList.add('selected');
        });
    });
});

function adicionarFeedback() {
    const nomeCompleto = document.getElementById('nome').value.trim();
    // Deriva nome e sobrenome a partir do campo único de nome (se houver espaço)
    let nome = nomeCompleto;
    let sobrenome = 'não informado';
    
    if (nomeCompleto.includes(' ')) {
        const partes = nomeCompleto.split(/\s+/);
        // Primeiro token como nome, restante como sobrenome
        nome = partes[0];
        sobrenome = partes.slice(1).join(' ').trim();
    }
    const mensagem = document.getElementById('mensagem').value.trim();
    const categoriaSelecionada = document.querySelector('#category-options .selected');
    const categoria = categoriaSelecionada ? categoriaSelecionada.textContent.trim().toLowerCase() : '';
    const privacidade = document.getElementById('privado').checked ? 'privado' : 'publico';

    if (!nomeCompleto || !nome) {
        mostrarMensagem('Por favor, preencha o nome!', false);
        return;
    }
    // Não exigimos sobrenome: ele é derivado/acrescentado automaticamente
    if (!mensagem) {
        mostrarMensagem('Por favor, escreva uma mensagem!', false);
        return;
    }
    if (!window.tipoSelecionado) {
        mostrarMensagem('Por favor, selecione o tipo de feedback!', false);
        return;
    }
    if (!categoria) {
        mostrarMensagem('Por favor, selecione a categoria!', false);
        return;
    }

    const titulo = `${categoria} ${nome}`;
    console.log('Tipo:', window.tipoSelecionado);
    console.log('Categoria:', categoria);
    console.log('Título:', titulo);
    console.log('Mensagem:', mensagem);

    // Buscar userId do localStorage (se o usuário estiver logado)
    const userId = localStorage.getItem('userId');
    const userType = localStorage.getItem('userType');
    
    console.log('UserId do localStorage:', userId);
    console.log('UserType do localStorage:', userType);

    // Payload compatível com Backend Node e Backend Java
    const payload = JSON.stringify({ 
        tipo: window.tipoSelecionado,
        categoria,
        titulo,
        mensagem,
        privacidade,
        nome,
        sobrenome,
        tipoUsuario: userType || 'aluno',
        userId: userId ? parseInt(userId) : null // Enviar userId se existir
    });

    // Usar apenas o backend Spring Boot (porta 8080)
    async function sendToSpring() {
        const url = 'http://localhost:8080/api/feedbacks';
        try {
            console.log('Enviando feedback para:', url);
            console.log('Payload:', payload);
            
            const res = await fetch(url, {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: payload
            });
            
            console.log('Status da resposta:', res.status, res.statusText);
            
            if (res.ok) {
                const feedbackSalvo = await res.json();
                console.log('Feedback salvo com sucesso:', feedbackSalvo);
                mostrarMensagem('Feedback enviado com sucesso!', true);
                // Limpar campos após sucesso
                document.getElementById('nome').value = '';
                document.getElementById('mensagem').value = '';
                document.querySelectorAll('.selected').forEach(el => el.classList.remove('selected'));
                document.querySelectorAll('.main-option').forEach(el => {
                    if (el.textContent.includes('Tipo')) el.textContent = 'Tipo de Feedback';
                    if (el.textContent.includes('Destinatário')) el.textContent = 'Destinatário';
                });
                window.tipoSelecionado = null;
                
                // Redirecionar após 1 segundo
                setTimeout(() => { 
                    // Usar caminho relativo ao invés de absoluto
                    window.location.href = 'mural.html'; 
                }, 1000);
                return;
            } else {
                let errorText = '';
                // Tentar obter mensagem de erro do header primeiro
                const errorHeader = res.headers.get('X-Error-Message');
                if (errorHeader) {
                    errorText = errorHeader;
                } else {
                    try { 
                        errorText = await res.text(); 
                        if (!errorText || errorText.trim() === '') {
                            errorText = 'Erro desconhecido do servidor';
                        }
                        console.error('Erro do servidor:', errorText);
                    } catch (e) { 
                        errorText = 'Erro ao ler resposta do servidor';
                        console.error('Erro ao ler resposta:', e);
                    }
                }
                console.error('Resposta não OK:', res.status, res.statusText, errorText);
                mostrarMensagem(`Erro ao salvar feedback (${res.status}): ${errorText}`, false);
            }
        } catch (e) {
            console.error('Erro ao conectar com o backend:', e);
            console.error('Tipo do erro:', e.name);
            console.error('Mensagem:', e.message);
            
            let mensagemErro = 'Falha ao conectar ao backend. ';
            
            if (e.name === 'TypeError' && e.message.includes('Failed to fetch')) {
                mensagemErro += 'Verifique se:\n';
                mensagemErro += '1. O Spring Boot está rodando na porta 8080\n';
                mensagemErro += '2. Não há firewall bloqueando a conexão\n';
                mensagemErro += '3. Você está acessando via servidor HTTP (não file://)';
            } else {
                mensagemErro += e.message;
            }
            
            mostrarMensagem(mensagemErro, false);
        }
    }

    sendToSpring();
}

// Função para mostrar mensagem na tela
function mostrarMensagem(texto, sucesso) {
    let msgDiv = document.getElementById('mensagem-envio');
    if (!msgDiv) {
        msgDiv = document.createElement('div');
        msgDiv.id = 'mensagem-envio';
        msgDiv.style.cssText = `
            margin-top: 15px;
            padding: 12px 15px;
            border-radius: 8px;
            font-size: 14px;
            line-height: 1.5;
            white-space: pre-line;
            word-wrap: break-word;
            max-width: 100%;
        `;
        document.querySelector('.feedback-container').appendChild(msgDiv);
    }
    
    // Usar innerHTML para preservar quebras de linha, mas sanitizar para segurança
    msgDiv.innerHTML = texto.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>');
    msgDiv.style.color = sucesso ? '#4caf50' : '#f44336';
    msgDiv.style.backgroundColor = sucesso ? 'rgba(76, 175, 80, 0.1)' : 'rgba(244, 67, 54, 0.1)';
    msgDiv.style.border = sucesso ? '1px solid #4caf50' : '1px solid #f44336';
    
    // Esconde a mensagem depois de 5 segundos (ou 8 para erros)
    const timeout = sucesso ? 3000 : 8000;
    setTimeout(() => { 
        msgDiv.innerHTML = ''; 
        msgDiv.style.border = 'none';
        msgDiv.style.backgroundColor = 'transparent';
    }, timeout);
}