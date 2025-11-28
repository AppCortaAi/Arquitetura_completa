import Styles from "./CSS/SignIn_inputs.module.css"
import { useState } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import { registerCustomer, registerBarber } from "../../services/authService"

function SignIn_inputs() {

    // Estados do formulário
    const [step, setStep] = useState(1); // <<< controla o progresso

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [cpf, setCpf] = useState("");

    const [tell, setTell] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [workStart, setWorkStart] = useState("09:00");
    const [workEnd, setWorkEnd] = useState("18:00");

    const [error, setError] = useState(null);

    const navigate = useNavigate();
    const location = useLocation();
    const userType = location.state?.role || "customer";


    const handleNextStep = (e) => {
        e.preventDefault();
        
        if (!name || !email || !cpf) {
            setError("Preencha todos os campos.");
            return;
        }

        setError(null);
        setStep(2);
    };


    const handleRegister = async (e) => {
        e.preventDefault();
        setError(null);

        if (password !== confirmPassword) {
            setError("As senhas não coincidem.");
            return;
        }

        try {
            if (userType === "customer") {
                await registerCustomer({
                    name, email, documentCPF: cpf, tell, password
                });
            } else {
                await registerBarber({
                    name, email, documentCPF: cpf, tell, password,
                    workStartTime: workStart,
                    workEndTime: workEnd,
                });
            }

            alert("Cadastro realizado com sucesso!");
            navigate("/login", { state: { role: userType } });

        } catch (err) {
            console.error(err);
            setError("Erro ao cadastrar. Verifique os dados.");
        }
    };


    return (
        <div className={Styles.SignIn_inputs_container}>

            <h3 className={Styles.title_register}>
                Cadastro de {userType === "barber" ? "Barbeiro" : "Cliente"}
            </h3>

            <form>
                {step === 1 && (
                    <>
                        <label className={Styles.label_name}>
                            <p>Nome Completo:</p>
                            <input 
                                id={Styles.name_input}
                                type="text"
                                value={name}
                                onChange={e => setName(e.target.value)}
                                placeholder="Digite seu nome completo"
                                required
                            />
                        </label>

                        <label className={Styles.label_email}>
                            <p>E-mail:</p>
                            <input 
                                id={Styles.email_input}
                                type="email"
                                value={email}
                                onChange={e => setEmail(e.target.value)}
                                placeholder="seuemail@exemplo.com"
                                required
                            />
                        </label>

                        <label className={Styles.label_name}>
                            <p>CPF:</p>
                            <input
                                 id={Styles.email_input}
                                type="text"
                                value={cpf}
                                onChange={e => setCpf(e.target.value)}
                                placeholder="Somente números"
                                required
                            />
                        </label>

                        {error && <p className={Styles.error_message}>{error}</p>}

                        <button className={Styles.SignIn_button} onClick={handleNextStep}>
                            Próximo
                        </button>
                    </>
                )}

                {step === 2 && (
                    <>
                        <label className={Styles.label_name}>
                            <p>Telefone:</p>
                            <input 
                             id={Styles.name_input}
                                type="text"
                                value={tell}
                                onChange={e => setTell(e.target.value)}
                                placeholder="11999999999"
                                required
                            />
                        </label>

                        {userType === "barber" && (
                            <>
                                <label className={Styles.label_name}>
                                    <p>Início de Expediente:</p>
                                    <input 
                                     id={Styles.name_input}
                                        type="time"
                                        value={workStart}
                                        onChange={e => setWorkStart(e.target.value)}
                                        required
                                    />
                                </label>

                                <label className={Styles.label_name}>
                                    <p>Fim de Expediente:</p>
                                    <input 
                                     id={Styles.name_input}
                                        type="time"
                                        value={workEnd}
                                        onChange={e => setWorkEnd(e.target.value)}
                                        required
                                    />
                                </label>
                            </>
                        )}

                        <label className={Styles.label_name}>
                            <p>Senha:</p>
                            <input 
                             id={Styles.name_input}
                                type="password"
                                value={password}
                                onChange={e => setPassword(e.target.value)}
                                required
                            />
                        </label>

                        <label className={Styles.label_name}>
                            <p>Confirmar senha:</p>
                            <input 
                             id={Styles.name_input}
                                type="password"
                                value={confirmPassword}
                                onChange={e => setConfirmPassword(e.target.value)}
                                required
                            />
                        </label>

                        {error && <p className={Styles.error_message}>{error}</p>}

                        <button className={Styles.SignIn_button} onClick={handleRegister}>
                            Cadastrar
                        </button>
                    </>
                )}
            </form>

            <p className={Styles.login_link}>Já possui conta? Entrar</p>
        </div>
    );
}

export default SignIn_inputs;
