import Styles from "./MenuNavBar.module"
function MenuNavBar() {
  return (
    <div className={Styles.MenuNavBar_container}>
        <div>
            <h3>Inicio</h3>
        </div>
         <div>
            <h3>Meus Atendimentos</h3>
        </div>
    </div>
  )
}

export default MenuNavBar