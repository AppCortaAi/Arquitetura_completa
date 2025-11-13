import Navbar from "../components/HomePage/Navbar"
import SearchBar from "../components/HomePage/SearchBar"
import Styles from "./CSS/HomePage.module.css"

function HomePage() {
  return (
    <div className={Styles.homepage_container}>
      <Navbar/>
      <SearchBar/>
        {/* <h1>Teste</h1> */}
    </div>
  )
}

export default HomePage