import SignIn_inputs from "../components/Sign_In/SignIn_inputs"
import Styles from "./CSS/SignInPage.module.css"

function SignInPage() {
    return (
        <div className={Styles.SignInPage_container}>
            <div className={Styles.SignInPage_title_container}>
                <div className={Styles.arrow_container}>
                    <img src="./Icons/left_arrow.png" alt="" />
                </div>
                <h3>Crie a Sua Conta</h3>
            </div>

            <SignIn_inputs/>

        </div>
    )
}

export default SignInPage