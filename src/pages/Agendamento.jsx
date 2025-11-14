import React, { useState } from "react";
import styles from "./CSS/Agendamento.module.css";

export default function Agendamento() {
  const [selectedService, setSelectedService] = useState("Corte de Cabelo");
  const [selectedBarber, setSelectedBarber] = useState(null);
  const [selectedTime, setSelectedTime] = useState(null);

  const services = ["Corte de Cabelo", "Barba", "Corte e Barba"];

  const barbers = [
    { id: 1, name: "João Silva", status: "Disponível", image: "/img1.jpg" },
    { id: 2, name: "Carlos Souza", status: "Disponível", image: "/img2.jpg" },
    { id: 3, name: "Pedro Almeida", status: "Ocupado", image: "/img3.jpg" },
  ];

  const times = ["09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00", "17:00"];

  const canConfirm = selectedService && selectedBarber && selectedTime;

  return (
    <div className={styles.container}>
      <h2 className={styles.title}>Agendamento</h2>

      {/* Serviços */}
      <section className={styles.section}>
        <h3>Selecione o Serviço</h3>
        <div className={styles.services}>
          {services.map(service => (
            <button
              key={service}
              className={`${styles.option} ${selectedService === service ? styles.active : ""}`}
              onClick={() => setSelectedService(service)}
            >
              {service}
            </button>
          ))}
        </div>
      </section>

      {/* Barbeiros */}
      <section className={styles.section}>
        <h3>Selecione o Barbeiro</h3>
        <div className={styles.barberGrid}>
          {barbers.map(barber => (
            <div
              key={barber.id}
              className={`${styles.barberCard} ${selectedBarber === barber.id ? styles.selected : ""}`}
              onClick={() => barber.status !== "Ocupado" && setSelectedBarber(barber.id)}
            >
              <img src={barber.image} alt={barber.name} />
              <p className={styles.name}>{barber.name}</p>
              <span
                className={`${styles.status} ${
                  barber.status === "Disponível" ? styles.available : styles.busy
                }`}
              >
                {barber.status}
              </span>
            </div>
          ))}
        </div>
      </section>

      {/* Horários */}
      <section className={styles.section}>
        <h3>Selecione a Data e Horário</h3>

        <div className={styles.timesGrid}>
          {times.map(time => (
            <button
              key={time}
              className={`${styles.time} ${selectedTime === time ? styles.activeTime : ""}`}
              onClick={() => setSelectedTime(time)}
            >
              {time}
            </button>
          ))}
        </div>
      </section>

      {/* Botão Final */}
      <button
        className={`${styles.confirmButton} ${!canConfirm ? styles.disabled : ""}`}
        disabled={!canConfirm}
      >
        Confirmar Agendamento
      </button>
    </div>
  );
}
