import { useState, useRef } from "react";
import API from "../services/api";

export default function AdminDashboard() {

    const [form, setForm] = useState({
        userId: "",
        specialization: "",
        workingHoursStart: "09:00",
        workingHoursEnd: "17:00",
        slotDurationMinutes: 30
    });

    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);
    const isSubmitting = useRef(false);

    // Leave Management
    const [leaveDoctorId, setLeaveDoctorId] = useState("");
    const [leaveDate, setLeaveDate] = useState("");
    const [leaveMsg, setLeaveMsg] = useState("");

    const createDoctor = async () => {

        if (isSubmitting.current) return;

        isSubmitting.current = true;
        setLoading(true);

        const request = {
            user: {
                id: parseInt(form.userId)
            },
            specialization: form.specialization,
            workingHoursStart: form.workingHoursStart,
            workingHoursEnd: form.workingHoursEnd,
            slotDurationMinutes: parseInt(form.slotDurationMinutes),
            leaveDays: []
        };

        try {

            await API.post("/api/admin/doctors", request);

            setMessage("Doctor profile created successfully!");

            setForm({
                userId: "",
                specialization: "",
                workingHoursStart: "09:00",
                workingHoursEnd: "17:00",
                slotDurationMinutes: 30
            });

        } catch (e) {

            console.log(e);

            setMessage(
                e.response?.data || "Failed to create doctor profile."
            );

        } finally {

            isSubmitting.current = false;
            setLoading(false);

        }
    };

    const markLeave = async () => {

        if (!leaveDoctorId || !leaveDate) {

            setLeaveMsg("Doctor ID and Leave Date are required.");

            return;
        }

        try {

            await API.post(
                `/api/admin/doctors/${leaveDoctorId}/leave`,
                {
                    date: leaveDate
                }
            );

            setLeaveMsg(
                "Leave marked successfully. Patients have been notified."
            );

            setLeaveDoctorId("");
            setLeaveDate("");

        } catch (e) {

            console.log(e);

            setLeaveMsg(
                e.response?.data || "Failed to mark leave."
            );

        }
    };

    return (

        <div style={styles.container}>

            <h2 style={styles.heading}>Admin Dashboard</h2>

            {message && (
                <p style={styles.success}>
                    {message}
                </p>
            )}

            {/* Create Doctor */}

            <div style={styles.card}>

                <h3>Create Doctor Profile</h3>

                <p style={styles.hint}>
                    Register the doctor user first, then enter the User ID here.
                </p>

                <input
                    style={styles.input}
                    placeholder="Doctor User ID"
                    value={form.userId}
                    onChange={(e) =>
                        setForm({
                            ...form,
                            userId: e.target.value
                        })
                    }
                />

                <input
                    style={styles.input}
                    placeholder="Specialization"
                    value={form.specialization}
                    onChange={(e) =>
                        setForm({
                            ...form,
                            specialization: e.target.value
                        })
                    }
                />

                <label style={styles.label}>
                    Working Hours Start
                </label>

                <input
                    style={styles.input}
                    type="time"
                    value={form.workingHoursStart}
                    onChange={(e) =>
                        setForm({
                            ...form,
                            workingHoursStart: e.target.value
                        })
                    }
                />

                <label style={styles.label}>
                    Working Hours End
                </label>

                <input
                    style={styles.input}
                    type="time"
                    value={form.workingHoursEnd}
                    onChange={(e) =>
                        setForm({
                            ...form,
                            workingHoursEnd: e.target.value
                        })
                    }
                />

                <input
                    style={styles.input}
                    type="number"
                    placeholder="Slot Duration (minutes)"
                    value={form.slotDurationMinutes}
                    onChange={(e) =>
                        setForm({
                            ...form,
                            slotDurationMinutes: e.target.value
                        })
                    }
                />

                <button
                    style={styles.button}
                    disabled={loading}
                    onClick={createDoctor}
                >
                    {loading ? "Creating..." : "Create Doctor"}
                </button>

            </div>

            {/* Leave Section */}

            <div style={styles.card}>

                <h3>Mark Doctor Leave</h3>

                <input
                    style={styles.input}
                    placeholder="Doctor ID"
                    value={leaveDoctorId}
                    onChange={(e) =>
                        setLeaveDoctorId(e.target.value)
                    }
                />

                <input
                    style={styles.input}
                    type="date"
                    value={leaveDate}
                    onChange={(e) =>
                        setLeaveDate(e.target.value)
                    }
                />

                <button
                    style={styles.button}
                    onClick={markLeave}
                >
                    Mark Leave & Notify Patients
                </button>

                {leaveMsg && (

                    <p style={styles.success}>
                        {leaveMsg}
                    </p>

                )}

            </div>

        </div>
    );
}

const styles = {

    container: {
        maxWidth: "700px",
        margin: "0 auto",
        padding: "2rem"
    },

    heading: {
        color: "#2d3748",
        marginBottom: "1.5rem"
    },

    card: {
        background: "white",
        padding: "1.5rem",
        borderRadius: "12px",
        boxShadow: "0 2px 8px rgba(0,0,0,0.08)",
        marginBottom: "2rem"
    },

    input: {
        width: "100%",
        padding: "0.75rem",
        marginBottom: "0.75rem",
        border: "1px solid #e2e8f0",
        borderRadius: "8px",
        fontSize: "1rem",
        boxSizing: "border-box"
    },

    label: {
        display: "block",
        marginBottom: "0.25rem",
        color: "#4a5568"
    },

    button: {
        width: "100%",
        padding: "0.8rem",
        background: "#4f46e5",
        color: "white",
        border: "none",
        borderRadius: "8px",
        cursor: "pointer",
        fontSize: "1rem"
    },

    success: {
        color: "green",
        marginTop: "1rem",
        fontWeight: "bold"
    },

    hint: {
        color: "#666",
        marginBottom: "1rem"
    }

}