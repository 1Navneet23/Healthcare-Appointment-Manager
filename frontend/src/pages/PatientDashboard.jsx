import { useState, useEffect,useRef } from 'react'
import API from '../services/api'

export default function PatientDashboard({ userId }) {
    const [specialization, setSpecialization] = useState('')
    const [doctors, setDoctors] = useState([])
    const [selectedDoctor, setSelectedDoctor] = useState(null)
    const [date, setDate] = useState('')
    const [slots, setSlots] = useState([])
    const [selectedSlot, setSelectedSlot] = useState('')
    const [symptoms, setSymptoms] = useState('')
    const [appointments, setAppointments] = useState([])
    const [message, setMessage] = useState('')

    useEffect(() => {
        fetchMyAppointments()
    }, [])

    const fetchMyAppointments = async () => {
        try {
            const res = await API.get(`/api/appointments/patient/${userId}`)
            setAppointments(res.data)
        } catch (e) { console.error(e) }
    }

    const searchDoctors = async () => {
        try {
            const res = await API.get(`/api/doctors?specialization=${specialization}`)
            setDoctors(res.data)
        } catch (e) { setMessage('Search failed') }
    }

    const fetchSlots = async (doctorId) => {
        try {
            const res = await API.get(`/api/doctors/${doctorId}/slots?date=${date}`)
            setSlots(res.data)
        } catch (e) { setMessage('Could not fetch slots') }
    }

    const isBooking = useRef(false)

    const bookAppointment = async () => {
        if (isBooking.current) return;
        isBooking.current = true;
        try {
            await API.post('/api/appointments/book', {
                patient: { id: userId },
                doctor: { id: selectedDoctor.id },
                appointmentDate: date,
                appointmentTime: selectedSlot,
                symptoms
            })
            setMessage('Appointment booked successfully!')
            fetchMyAppointments()
        } catch (e) {
            setMessage(e.response?.data || 'Booking failed')
        } finally {
            isBooking.current = false;
        }
    }

    const cancelAppointment = async (id) => {
        try {
            await API.put(`/api/appointments/${id}/cancel`)
            setMessage('Appointment cancelled')
            fetchMyAppointments()
        } catch (e) { setMessage('Cancel failed') }
    }

    return (
        <div style={styles.container}>
            <h2 style={styles.heading}>Patient Dashboard</h2>
            {message && <p style={styles.message}>{message}</p>}

            <div style={styles.section}>
                <h3>Search Doctors</h3>
                <input style={styles.input} placeholder="Specialization (e.g. Cardiology)"
                       value={specialization} onChange={e => setSpecialization(e.target.value)} />
                <button style={styles.button} onClick={searchDoctors}>Search</button>
            </div>

            {doctors.length > 0 && (
                <div style={styles.section}>
                    <h3>Select a Doctor</h3>
                    {doctors.map(doc => (
                        <div key={doc.id} style={{
                            ...styles.card,
                            border: selectedDoctor?.id === doc.id ? '2px solid #4299e1' : '1px solid #e2e8f0'
                        }} onClick={() => setSelectedDoctor(doc)}>
                            <p><strong>Dr. {doc.user.name}</strong></p>
                            <p>{doc.specialization}</p>
                            <p>Slot: {doc.slotDurationMinutes} mins</p>
                        </div>
                    ))}
                </div>
            )}

            {selectedDoctor && (
                <div style={styles.section}>
                    <h3>Pick a Date & Slot</h3>
                    <input style={styles.input} type="date" value={date}
                           onChange={e => setDate(e.target.value)} />
                    <button style={styles.button}
                            onClick={() => fetchSlots(selectedDoctor.id)}>Get Slots</button>
                    {slots.length > 0 && (
                        <div style={styles.slotGrid}>
                            {slots.map(slot => (
                                <div key={slot} style={{
                                    ...styles.slot,
                                    background: selectedSlot === slot ? '#4299e1' : '#ebf8ff',
                                    color: selectedSlot === slot ? 'white' : '#2b6cb0'
                                }} onClick={() => setSelectedSlot(slot)}>
                                    {slot}
                                </div>
                            ))}
                        </div>
                    )}
                    {selectedSlot && (
                        <div>
              <textarea style={{...styles.input, height:'80px'}}
                        placeholder="Describe your symptoms..."
                        value={symptoms} onChange={e => setSymptoms(e.target.value)} />
                            <button style={styles.button} onClick={bookAppointment}>
                                Confirm Booking
                            </button>
                        </div>
                    )}
                </div>
            )}

            <div style={styles.section}>
                <h3>My Appointments</h3>
                {appointments.length === 0 && <p>No appointments yet.</p>}
                {appointments.map(apt => (
                    <div key={apt.id} style={styles.card}>
                        <p><strong>Dr. {apt.doctor?.user?.name}</strong> — {apt.appointmentDate} at {apt.appointmentTime}</p>
                        <p>Status: <span style={{color: apt.status === 'CONFIRMED' ? 'green' : 'red'}}>{apt.status}</span></p>
                        {apt.preVisitSummary && (
                            <div style={styles.summary}>
                                <strong>AI Summary:</strong>
                                <p>{apt.preVisitSummary}</p>
                            </div>
                        )}
                        {apt.postVisitSummary && (
                            <div style={{
                                background:'#f0fff4',
                                padding:'0.75rem',
                                borderRadius:'6px',
                                marginTop:'0.5rem',
                                fontSize:'0.9rem'
                            }}>
                                <strong>Post-Visit Summary from Doctor:</strong>
                                <p>{apt.postVisitSummary}</p>
                            </div>
                        )}

                        {apt.doctorNotes && (
                            <div style={{
                                background:'#fffaf0',
                                padding:'0.75rem',
                                borderRadius:'6px',
                                marginTop:'0.5rem',
                                fontSize:'0.9rem'
                            }}>
                                <strong>Doctor Notes:</strong>
                                <p>{apt.doctorNotes}</p>
                            </div>
                        )}

                        {apt.status === 'CONFIRMED' && (
                            <button style={styles.cancelBtn} onClick={() => cancelAppointment(apt.id)}>
                                Cancel
                            </button>
                        )}
                    </div>
                ))}
            </div>
        </div>
    )
}

const styles = {
    container: { maxWidth:'800px', margin:'0 auto', padding:'2rem' },
    heading: { color:'#2d3748', marginBottom:'1.5rem' },
    section: { background:'white', padding:'1.5rem', borderRadius:'12px',
        marginBottom:'1.5rem', boxShadow:'0 2px 8px rgba(0,0,0,0.08)' },
    input: { width:'100%', padding:'0.75rem', marginBottom:'0.75rem',
        border:'1px solid #e2e8f0', borderRadius:'8px',
        fontSize:'1rem', boxSizing:'border-box' },
    button: { padding:'0.75rem 1.5rem', background:'#4299e1', color:'white',
        border:'none', borderRadius:'8px', cursor:'pointer', marginBottom:'0.5rem' },
    cancelBtn: { padding:'0.5rem 1rem', background:'#fc8181', color:'white',
        border:'none', borderRadius:'8px', cursor:'pointer', marginTop:'0.5rem' },
    card: { padding:'1rem', border:'1px solid #e2e8f0', borderRadius:'8px',
        marginBottom:'0.75rem', cursor:'pointer' },
    slotGrid: { display:'flex', flexWrap:'wrap', gap:'0.5rem', margin:'0.75rem 0' },
    slot: { padding:'0.5rem 1rem', borderRadius:'6px', cursor:'pointer',
        fontWeight:'500' },
    message: { color:'#4299e1', fontWeight:'500', marginBottom:'1rem' },
    summary: { background:'#f7fafc', padding:'0.75rem', borderRadius:'6px',
        marginTop:'0.5rem', fontSize:'0.9rem' }
}