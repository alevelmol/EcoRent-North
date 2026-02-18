import { useState } from "react";
import api from "../../api/axiosConfig";

function ReportsView() {

  // =============================
  // STATES
  // =============================
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [incomeReport, setIncomeReport] = useState(null);
  const [topEquipments, setTopEquipments] = useState([]);
  const [topClients, setTopClients] = useState([]);
  const [loading, setLoading] = useState(false);

  // =============================
  // CONSULTAR INGRESOS
  // =============================
  const getIncome = () => {

    if (!startDate || !endDate) {
      alert("Selecciona un rango de fechas");
      return;
    }

    setLoading(true);

    api.get(`/reports/income?start=${startDate}&end=${endDate}`)
      .then(res => {
        console.log("Income response:", res.data);
        setIncomeReport(res.data);   // ✅ Guardamos objeto completo
      })
      .catch(err => {
        console.error(err);
        alert("Error al consultar ingresos");
      })
      .finally(() => setLoading(false));
  };

  // =============================
  // EQUIPOS MÁS ALQUILADOS
  // =============================
  const getTopEquipments = () => {

    setLoading(true);

    api.get("/reports/top-equipments")
      .then(res => setTopEquipments(res.data))
      .catch(err => {
        console.error(err);
        alert("Error al consultar equipos");
      })
      .finally(() => setLoading(false));
  };

  // =============================
  // CLIENTES RECURRENTES
  // =============================
  const getTopClients = () => {

    setLoading(true);

    api.get("/reports/top-clients")
      .then(res => setTopClients(res.data))
      .catch(err => {
        console.error(err);
        alert("Error al consultar clientes");
      })
      .finally(() => setLoading(false));
  };

  return (
    <div className="card">
      <h2>Reportes Estratégicos</h2>

      {/* ===================================== */}
      {/* INGRESOS POR PERIODO */}
      {/* ===================================== */}
      <section>
        <h3>Ingresos por periodo</h3>

        <div style={{ marginBottom: "10px" }}>
          <label>Desde: </label>
          <input
            type="date"
            value={startDate}
            onChange={e => setStartDate(e.target.value)}
          />

          <label style={{ marginLeft: "10px" }}>Hasta: </label>
          <input
            type="date"
            value={endDate}
            onChange={e => setEndDate(e.target.value)}
          />

          <button
            onClick={getIncome}
            style={{ marginLeft: "10px" }}
          >
            Consultar
          </button>
        </div>

        {loading && <p>Cargando...</p>}

        {incomeReport && (
          <div style={{
            background: "#f5f5f5",
            padding: "12px",
            borderRadius: "6px",
            marginTop: "10px"
          }}>
            <p>
              <strong>Desde:</strong> {startDate}
            </p>
            <p>
              <strong>Hasta:</strong> {endDate}
            </p>
            <p>
              <strong>Total Ingresos:</strong>{" "}
              {Number(incomeReport.totalIncome).toFixed(2)} €
            </p>
          </div>
        )}
      </section>

      <hr />

      {/* ===================================== */}
      {/* EQUIPOS MÁS ALQUILADOS */}
      {/* ===================================== */}
      <section>
        <h3>Equipos más alquilados</h3>

        <button onClick={getTopEquipments}>
          Consultar
        </button>

        {topEquipments.length > 0 && (
          <table style={{ marginTop: "10px", width: "100%" }}>
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Categoría</th>
                <th>Código</th>
                <th>Precio/día</th>
              </tr>
            </thead>
            <tbody>
              {topEquipments.map(eq => (
                <tr key={eq.id}>
                  <td>{eq.name}</td>
                  <td>{eq.category}</td>
                  <td>{eq.internalCode}</td>
                  <td>{eq.pricePerDay} €</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <hr />

      {/* ===================================== */}
      {/* CLIENTES RECURRENTES */}
      {/* ===================================== */}
      <section>
        <h3>Clientes recurrentes</h3>

        <button onClick={getTopClients}>
          Consultar
        </button>

        {topClients.length > 0 && (
          <table style={{ marginTop: "10px", width: "100%" }}>
            <thead>
              <tr>
                <th>Nombre</th>
                <th>DNI</th>
                <th>Email</th>
                <th>Teléfono</th>
              </tr>
            </thead>
            <tbody>
              {topClients.map(client => (
                <tr key={client.id}>
                  <td>{client.name}</td>
                  <td>{client.dni}</td>
                  <td>{client.email}</td>
                  <td>{client.phone}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

    </div>
  );
}

export default ReportsView;
