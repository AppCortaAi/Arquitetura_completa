import Login_Inputs from "../components/Login/Login_Inputs"
import Styles from "./CSS/LoginPage.module.css"

function LoginPage() {
    return (
        <div className={Styles.LoginPage_container}>
            <div className={Styles.content_container}>
                <div className={Styles.logo_container}>
                    Logo
                </div>
                <h1 className={Styles.title_login}>Acesse sua Conta</h1>
            </div>

            <Login_Inputs/>

            <div>
                <h3>Não tem uma conta? Crie uma Agora</h3>
            </div>
        </div>
    )
}

export default LoginPage