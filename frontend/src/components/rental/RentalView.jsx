import { useState, useEffect } from "react";
import api from "../../api/axiosConfig";

function RentalView() {

  const [form, setForm] = useState({
    clientDni: "",
    equipmentId: "",
    startDate: "",
    endDate: ""
  });

  const [clients, setClients] = useState([]);
  const [selectedDni, setSelectedDni] = useState("");
  const [rentals, setRentals] = useState([]);

  // Cargar clientes al iniciar
  useEffect(() => {
    api.get("/clients")
      .then(res => setClients(res.data))
      .catch(err => console.error(err));
  }, []);

  // Crear alquiler
  const handleSubmit = () => {
    api.post("/rentals", form)
      .then(() => {
        alert("Alquiler creado");
        setForm({
          clientDni: "",
          equipmentId: "",
          startDate: "",
          endDate: ""
        });
      })
      .catch(err => alert(err.response?.data));
  };

  // Buscar alquileres por DNI
  const searchRentals = () => {
    if (!selectedDni) {
      alert("Selecciona un DNI");
      return;
    }

    api.get(`/clients/${selectedDni}/rentals`)
      .then(res => setRentals(res.data))
      .catch(err => alert(err.response?.data));
  };

  return (
    <div className="card">

      <h2>Crear Alquiler</h2>

      <input
        placeholder="DNI Cliente"
        value={form.clientDni}
        onChange={e => setForm({ ...form, clientDni: e.target.value })}
      />

      <input
        placeholder="ID Equipo"
        value={form.equipmentId}
        onChange={e => setForm({ ...form, equipmentId: e.target.value })}
      />

      <input
        type="date"
        value={form.startDate}
        onChange={e => setForm({ ...form, startDate: e.target.value })}
      />

      <input
        type="date"
        value={form.endDate}
        onChange={e => setForm({ ...form, endDate: e.target.value })}
      />

      <button className="primary" onClick={handleSubmit}>
        Registrar Alquiler
      </button>

      <hr />

      <h2>Buscar Alquileres por Cliente</h2>

      <select
        value={selectedDni}
        onChange={e => setSelectedDni(e.target.value)}
      >
        <option value="">Selecciona un DNI</option>
        {clients.map(client => (
          <option key={client.id} value={client.dni}>
            {client.dni} - {client.name}
          </option>
        ))}
      </select>

      <button className="secondary" onClick={searchRentals}>
        Buscar
      </button>

      <hr />

      {rentals.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Equipo</th>
              <th>Inicio</th>
              <th>Fin</th>
              <th>Importe</th>
              <th>Devuelto</th>
            </tr>
          </thead>
          <tbody>
            {rentals.map(rental => (
              <tr key={rental.id}>
                <td>{rental.id}</td>
                <td>{rental.equipmentName}</td>
                <td>{rental.startDate}</td>
                <td>{rental.endDate}</td>
                <td>{rental.totalAmount}</td>
                <td>{rental.returned ? "Sí" : "No"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

    </div>
  );
}

export default RentalView;
