import Barbershops from "../components/HomePage/Barbershops/Barbershops"
import Favorite_barbershops from "../components/HomePage/Favorite_barbershops/Favorite_barbershops"
import Navbar from "../components/HomePage/Navbar"
import SearchBar from "../components/HomePage/SearchBar"
import Styles from "./CSS/HomePage.module.css"

function HomePage() {
  return (
    <div className={Styles.homepage_container}>
      <Navbar/>
      <SearchBar/>
      <Favorite_barbershops/>
      <Barbershops/>
        {/* <h1>Teste</h1> */}
    </div>
  )
}

export default HomePage