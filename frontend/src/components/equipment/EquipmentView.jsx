import { useEffect, useState } from "react";
import api from "../../api/axiosConfig";

function EquipmentView() {

  const [equipments, setEquipments] = useState([]);
  const [editingId, setEditingId] = useState(null);

  const [form, setForm] = useState({
    name: "",
    category: "",
    internalCode: "",
    pricePerDay: ""
  });

  useEffect(() => { loadEquipments(); }, []);

  const loadEquipments = async () => {
    const res = await api.get("/equipments");
    setEquipments(res.data);
  };

  const handleSubmit = async () => {

    const payload = {
      ...form,
      pricePerDay: parseFloat(form.pricePerDay)
    };

    if (editingId) {
      await api.put(`/equipments/${editingId}`, payload);
    } else {
      await api.post("/equipments", payload);
    }

    setForm({ name: "", category: "", internalCode: "", pricePerDay: "" });
    setEditingId(null);
    loadEquipments();
  };

  const changeStatus = async (id, status) => {
    await api.put(`/equipments/${id}/status`, { status });
    loadEquipments();
  };

  const editEquipment = (eq) => {
    setForm(eq);
    setEditingId(eq.id);
  };

  return (
    <div className="card">
      <h2>Gestión de Equipos</h2>

      <div className="form-grid">
        <input placeholder="Nombre" value={form.name}
          onChange={e => setForm({...form, name: e.target.value})} />

        <input placeholder="Categoría" value={form.category}
          onChange={e => setForm({...form, category: e.target.value})} />

        <input placeholder="Código interno" value={form.internalCode}
          onChange={e => setForm({...form, internalCode: e.target.value})} />

        <input type="number" placeholder="Precio por día"
          value={form.pricePerDay}
          onChange={e => setForm({...form, pricePerDay: e.target.value})} />
      </div>

      <button className="primary" onClick={handleSubmit}>
        {editingId ? "Actualizar" : "Registrar"}
      </button>

      <hr />

      <div className="equipment-grid">
        {equipments.map(eq => (
          <div key={eq.id} className="equipment-card">
            <h4>{eq.name}</h4>
            <p>{eq.category}</p>
            <p><strong>{eq.pricePerDay}€ / día</strong></p>
            <span className={`status-${eq.status}`}>
              {eq.status}
            </span>

            <div className="button-group">
              <button className="secondary" onClick={() => editEquipment(eq)}>
                Editar
              </button>
              <button className="success"
                onClick={() => changeStatus(eq.id, "AVAILABLE")}>
                Disponible
              </button>
              <button className="danger"
                onClick={() => changeStatus(eq.id, "MAINTENANCE")}>
                Mantenimiento
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default EquipmentView;
