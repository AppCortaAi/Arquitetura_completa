import Styles from "./CSS/ServicesAgendamento.module.css"
import { useState } from "react";

function ServicesAgendamento() {

    const [selectedService, setSelectedService] = useState("Corte de Cabelo");
    const services = ["Corte de Cabelo", "Barba", "Corte e Barba"];

    return (
        <div className={Styles.section}>
            <h3>Selecione o Serviço</h3>
            <div className={Styles.services}>
                {services.map(service => (
                    <button
                        key={service}
                        className={`${Styles.option} ${selectedService === service ? Styles.active : ""}`}
                        onClick={() => setSelectedService(service)}
                    >
                        {service}
                    </button>
                ))}
            </div>

        </div>
    )
}

export default ServicesAgendamento