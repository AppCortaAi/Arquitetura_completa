import Carousel_barbershops from "./carousel_barbershops"
import Styles from "./CSS/Favorite_barbershops.module.css"

function Favorite_barbershops() {
    return (
        <div className={Styles.favorite_barbershops_container}>
            <h3>Minhas Barbearias Favoritas</h3>
            <Carousel_barbershops/>
        </div>
    )
}

export default Favorite_barbershops