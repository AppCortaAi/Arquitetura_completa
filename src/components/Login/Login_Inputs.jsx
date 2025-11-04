import Styles from "./CSS/Login_inputs.module.css"

function Login_Inputs() {
    return (
        <div className={Styles.Login_Inputs_container}>
            <form action="submit">
                <label className={Styles.label_email}>
                    <p className={Styles.label_input}>Email</p>
                    <input type="email" name="email_area" id={Styles.email_input} 
                    placeholder="Digite seu Email"/>
                </label>

                <label className={Styles.label_password}>
                    <p className={Styles.label_input}>Senha</p>
                    <input type="password" name="password_area" id={Styles.password_input} 
                    placeholder="Digite a sua Senha"/>
                    <p className={Styles.forgot_password_text}>Esqueceu a Senha?</p>
                </label>

                

                <button className={Styles.Login_button}>Entrar</button>

            </form>
        </div>
    )
}

export default Login_Inputs