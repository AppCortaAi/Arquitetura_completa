import Container_Barbericons from "./Container_Barbericons"
import Styles from "./CSS/Barbershops.module.css"

function Barbershops() {
  return (
    <div className={Styles.barbershops_container}>
        <h3>Barbearias Disponiveis</h3>
        <Container_Barbericons/>
    </div>
  )
}

export default Barbershops