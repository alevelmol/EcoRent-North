import { useEffect, useState } from "react";
import api from "../../api/axiosConfig";

function Dashboard() {

  const [equipments, setEquipments] = useState([]);

  useEffect(() => {
    api.get("/equipments")
      .then(res => setEquipments(res.data))
      .catch(err => console.error(err));
  }, []);

  const available = equipments.filter(e => e.status === "AVAILABLE").length;
  const maintenance = equipments.filter(e => e.status === "MAINTENANCE").length;

  return (
    <div className="card">
      <h2>Dashboard General</h2>

      <div className="stats-grid">
        <div className="stat-box primary">
          <h3>{equipments.length}</h3>
          <p>Total Equipos</p>
        </div>

        <div className="stat-box success">
          <h3>{available}</h3>
          <p>Disponibles</p>
        </div>

        <div className="stat-box danger">
          <h3>{maintenance}</h3>
          <p>En mantenimiento</p>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
