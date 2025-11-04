import '../App.css'
import { useNavigate } from 'react-router-dom';

function StartPage() {

  const navigate = useNavigate();

  const handleNavigationSignIn = () => {
    navigate('/redirection');
  };

  const handleNavigationLogin = () => {
    navigate('/redirection');
  };

  return (
    <div className='workscreen'>
      <div className='start_container'>
        <div className='image_container'>
          <p>Logo</p>
        </div>

        <h1 className='title_app'>CortaAI</h1>

        <div className='text_container'>
          <h1 className='slogan'>Seu Estilo, Sua Hora marcada.</h1>
          <p className='first_text'>Agende seu corte de cabelo ou barba com seu barbeiro favorito de forma rápida e fácil</p>
        </div>

        <div className='button_container'>
          <button className='start_login_button' onClick={handleNavigationLogin}>Login</button>
          <button className='start_sign_in_button' onClick={handleNavigationSignIn}>Cadastre-se</button>
        </div>
      </div>
    </div>
  )
}

export default StartPage
