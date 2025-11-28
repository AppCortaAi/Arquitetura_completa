import Login_Inputs from "../components/Login/Login_Inputs"
import Styles from "./CSS/LoginPage.module.css"
import { Link } from "react-router-dom";

function LoginPage() {
    return (
        <div className={Styles.LoginPage_container}>
            <div className={Styles.content_container}>
                <div className={Styles.logo_container}>
                     <img src="./Icons/cortaAi.jpg" alt="Logo APP" />
                </div>
                <h1 className={Styles.title_login}>Acesse sua Conta</h1>
            </div>

            <Login_Inputs/>

            <div>
                <h3>Não tem uma conta? <Link className={Styles.Link} to="/identificacao">Crie uma Agora</Link></h3>
            </div>
        </div>
    )
}

export default LoginPage