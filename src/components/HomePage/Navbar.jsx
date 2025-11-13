import Styles from "./CSS/Navbar.module.css"

function Navbar() {
  return (
    <div className={Styles.navbar_container}>
        <h3>CortaAI</h3>
        <button><img src="./Icons/profile_icon2.png" alt="Imagem de Perfil" /></button>
    </div>
  )
}

export default Navbar