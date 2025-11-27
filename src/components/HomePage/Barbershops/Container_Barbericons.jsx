import { useState } from "react";
import Styles from "./CSS/ContainerBarberIcons.module.css"
import { useNavigate } from "react-router-dom";

function Container_Barbericons({ name, address, image, id}) {

  const navigate = useNavigate();
  const handleClick = () =>{
    navigate(`/agendamentoPage/${id}`);
  }

  return (
    <>
      <div className={Styles.barbershopsicons_container} onClick={handleClick}>
        <div className={Styles.image_icon_barbershop_container}>
          <img
            src={image}
            alt={`Logo da ${name}`}
            onError={(e) => { e.target.src = "./barbershop.png"; }} 
          />
        </div>

        <div className={Styles.text_icon_barbershop_container}>
          <h4>{name}</h4>
          <p>{address}</p>
        </div>
      </div>


    </>
  )
}

export default Container_Barbericons