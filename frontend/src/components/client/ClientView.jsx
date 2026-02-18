import { useState, useEffect } from "react";
import api from "../../api/axiosConfig";

function ClientView() {

  const [form, setForm] = useState({
    name: "",
    dni: "",
    phone: "",
    email: ""
  });

  const [clients, setClients] = useState([]);

  // Cargar clientes al iniciar
  useEffect(() => {
    loadClients();
  }, []);

  const loadClients = () => {
    api.get("/clients")
      .then(res => setClients(res.data))
      .catch(err => console.error(err));
  };

  const handleSubmit = () => {
    api.post("/clients", form)
      .then(() => {
        alert("Cliente registrado");
        setForm({ name: "", dni: "", phone: "", email: "" });
        loadClients(); // Recargar lista
      })
      .catch(err => alert(err.response?.data));
  };

  return (
    <div className="card">
      <h2>Registrar Cliente</h2>

      <input
        placeholder="Nombre"
        value={form.name}
        onChange={e => setForm({ ...form, name: e.target.value })}
      />

      <input
        placeholder="DNI"
        value={form.dni}
        onChange={e => setForm({ ...form, dni: e.target.value })}
      />

      <input
        placeholder="Teléfono"
        value={form.phone}
        onChange={e => setForm({ ...form, phone: e.target.value })}
      />

      <input
        placeholder="Email"
        value={form.email}
        onChange={e => setForm({ ...form, email: e.target.value })}
      />

      <button onClick={handleSubmit}>Registrar Cliente</button>

      <hr />

      <h2>Lista de Clientes</h2>

      {clients.length === 0 ? (
        <p>No hay clientes registrados.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Nombre</th>
              <th>DNI</th>
              <th>Teléfono</th>
              <th>Email</th>
            </tr>
          </thead>
          <tbody>
            {clients.map(client => (
              <tr key={client.id}>
                <td>{client.name}</td>
                <td>{client.dni}</td>
                <td>{client.phone}</td>
                <td>{client.email}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default ClientView;
