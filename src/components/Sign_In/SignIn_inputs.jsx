import Styles from "./CSS/SignIn_inputs.module.css"

function SignIn_inputs() {
  return (
    <div className={Styles.SignIn_inputs_container}>
        <form action="">
            <label className={Styles.label_name}>
                <p>Nome Completo:</p>
                <input type="text" name="name_area" id={Styles.name_input} 
                placeholder="Digite o seu Nome Completo"/>
            </label>

            <label className={Styles.label_email}>
                <p>E-mail:</p>
                <input type="email" name="email_area" id={Styles.email_input} 
                placeholder="seuemail@exemplo.com"/>
            </label>

            <label className={Styles.label_password}>
                <p>Digite a sua senha:</p>
                <input type="password" name="password_area" id={Styles.password_input} 
                placeholder="Digite uma senha forte"
                />
            </label>

            <label className={Styles.label_confirm_password}>
                <p>Confirme a sua Senha:</p>
                <input type="password" name="confirm_password_area" id={Styles.confirm_password_input} 
                placeholder="Confirme a sua senha"/>
            </label>

            <button className={Styles.SignIn_button}>Entrar</button>
        </form>

        <p className={Styles.login_link}>Já possuí conta? Entrar</p>
    </div>
  )
}

export default SignIn_inputs