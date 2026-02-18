import { useState } from "react";
import api from "../../api/axiosConfig";

function PaymentSection() {

  const [rentalId, setRentalId] = useState("");
  const [amount, setAmount] = useState("");
  const [paymentResult, setPaymentResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleRegisterPayment = () => {

    if (!rentalId || !amount) {
      alert("Introduce ID de alquiler e importe");
      return;
    }

    if (Number(amount) <= 0) {
      alert("El importe debe ser mayor que 0");
      return;
    }

    setLoading(true);

    api.post(`/rentals/${rentalId}/payments`, {
      amount: Number(amount)
    })
    .then(res => {
      setPaymentResult(res.data);
      setAmount("");
    })
    .catch(err => {
      alert(err.response?.data || "Error al registrar pago");
    })
    .finally(() => setLoading(false));
  };

  return (
    <div className="card">

      <h2>Registrar Pago</h2>

      <div style={{ marginBottom: "10px" }}>
        <input
          type="number"
          placeholder="ID del alquiler"
          value={rentalId}
          onChange={e => setRentalId(e.target.value)}
        />

        <input
          type="number"
          step="0.01"
          placeholder="Importe"
          value={amount}
          onChange={e => setAmount(e.target.value)}
          style={{ marginLeft: "10px" }}
        />

        <button
          onClick={handleRegisterPayment}
          style={{ marginLeft: "10px" }}
        >
          Registrar
        </button>
      </div>

      {loading && <p>Procesando pago...</p>}

      {paymentResult && (
        <div style={{
          marginTop: "15px",
          background: "#f5f5f5",
          padding: "12px",
          borderRadius: "6px"
        }}>
          <h3>Pago Registrado</h3>

          <p><strong>ID Pago:</strong> {paymentResult.id}</p>
          <p><strong>Importe:</strong> {paymentResult.amount} €</p>
          <p><strong>Fecha:</strong> {paymentResult.paymentDate}</p>
          <p>
            <strong>Estado del Alquiler:</strong>{" "}
            {paymentResult.status}
          </p>
        </div>
      )}

    </div>
  );
}

export default PaymentSection;
