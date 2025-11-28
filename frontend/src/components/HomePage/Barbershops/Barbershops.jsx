import Container_Barbericons from "./Container_Barbericons"
import Styles from "./CSS/Barbershops.module.css"
import { getAllBarbershops } from "../../../services/barbershopService"
import { useEffect, useState } from "react"


function Barbershops() {
  const [barbershops, setBarbershops] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAllBarbershops = async () => {
      const data = await getAllBarbershops();
      setBarbershops(data);
      setLoading(false);
    }

    fetchAllBarbershops()
  }, []);


  return (
    <div className={Styles.barbershops_container}>
      {loading ? (
        <p>Carregando Barbearias...</p>
      ) : barbershops.length > 0 ? (
        barbershops.map((shop) => (
          <Container_Barbericons 
          key={shop.id}
          name={shop.name}
          address={shop.address}
          image={shop.logoUrl || "./barbershop.jpg"}
          id={shop.id} />
        ))
      ) : (
        <p>Nenhuma barbearia encontrada.</p>
      )}

    </div>
  )
}

export default Barbershops