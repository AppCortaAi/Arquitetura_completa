import Header from "../components/AgendamentoPage/Header"
import ServicesAgendamento from "../components/AgendamentoPage/ServicesAgendamento"
import Styles from "./CSS/AgendamentoPage.module.css"

function AgendamentoPage() {
  return (
    <div className={Styles.agendamentoPage_container}>
        <Header/>
        <ServicesAgendamento/>
    </div>
  )
}

export default AgendamentoPage