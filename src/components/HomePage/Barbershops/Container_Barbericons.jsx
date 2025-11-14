import Styles from "./CSS/ContainerBarberIcons.module.css"

function Container_Barbericons() {
  return (
    <>
      <div className={Styles.barbershopsicons_container}>
        <div className={Styles.image_icon_barbershop_container}>
          <img src="./barbershop.jpg" alt="Imagem da barbearia" />
        </div>

        <div className={Styles.text_icon_barbershop_container}>
          <h4>Nome da Barbearia</h4>
          <p>Centro-SP</p>
        </div>
      </div>

      <div className={Styles.barbershopsicons_container}>
        <div className={Styles.image_icon_barbershop_container}>
          <img src="./barbershop.jpg" alt="Imagem da barbearia" />
        </div>

        <div className={Styles.text_icon_barbershop_container}>
          <h4>Nome da Barbearia</h4>
          <p>Centro-SP</p>
        </div>
      </div>

      
    </>
  )
}

export default Container_Barbericons