import Styles from "./MenuNavBar.module.css"
function MenuNavBar() {
  return (
    <div className={Styles.MenuNavBar_container}>
        <div>
            <h4>Inicio</h4>
        </div>
         <div>
            <h4>Meus Atendimentos</h4>
        </div>
    </div>
  )
}

export default MenuNavBar