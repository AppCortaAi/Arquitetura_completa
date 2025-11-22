import Styles from "./CSS/Login_inputs.module.css"
import { useState } from "react"
import { useNavigate } from "react-router-dom"

function Login_Inputs() {

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault(); // Evita recarregar a página

        try {
            // Tenta logar como cliente (ajuste a rota se for para barbeiros)
            const response = await api.post("/customers/login", {
                email: email,
                password: password
            });

            // O Back-end retorna { token: "...", userData: {...} }
            const { token, userData } = response.data;

            // Salva no navegador para usar nas outras telas
            localStorage.setItem("token", token);
            localStorage.setItem("user", JSON.stringify(userData));

            alert("Login realizado com sucesso!");
            navigate("/homepage"); // Redireciona

        } catch (error) {
            console.error(error);
            alert("Erro ao fazer login. Verifique email e senha.");
        }
    };

    return (
        <div className={Styles.Login_Inputs_container}>
            <form action="submit">
                <label className={Styles.label_email}>
                    <p className={Styles.label_input}>Email</p>
                    <input type="email" name="email_area" id={Styles.email_input}
                        placeholder="Digite seu Email" value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required />
                </label>

                <label className={Styles.label_password}>
                    <p className={Styles.label_input}>Senha</p>
                    <input type="password" name="password_area" id={Styles.password_input}
                        placeholder="Digite a sua Senha" value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required />
                    <p className={Styles.forgot_password_text}>Esqueceu a Senha?</p>
                </label>



                <button type="submit" onClick={handleLogin} className={Styles.Login_button}>Entrar</button>

            </form>
        </div>
    )
}

export default Login_Inputs