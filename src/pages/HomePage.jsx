import Navbar from "../components/HomePage/Navbar"
import Styles from "./CSS/HomePage.module.css"

function HomePage() {
  return (
    <div className={Styles.homepage_container}>
      <Navbar/>
        {/* <h1>Teste</h1> */}
    </div>
  )
}

export default HomePage