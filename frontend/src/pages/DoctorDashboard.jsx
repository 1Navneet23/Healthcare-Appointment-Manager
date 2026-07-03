import { useState, useEffect } from 'react'
import API from '../services/api'

export default function DoctorDashboard({ userId }) {
    const [appointments, setAppointments] = useState([])
    const [notes, setNotes] = useState({})
    const [message, setMessage] = useState('')

    useEffect(() => {
        fetchAppointments()
    }, [])

    const fetchAppointments = async () => {
        try {
            const res = await API.get(`/api/appointments/doctor/${userId}`)
            setAppointments(res.data)
        } catch (e) { console.error(e) }
    }

    const submitNotes = async (appointmentId) => {
        try {
            await API.put(`/api/appointments/${appointmentId}/notes?notes=${encodeURIComponent(notes[appointmentId] || '')}`)
            setMessage('Notes submitted and AI summary generated!')
            fetchAppointments()
        } catch (e) { setMessage('Failed to submit notes') }
    }

    return (
        <div style={styles.container}>
            <h2 style={styles.heading}>Doctor Dashboard</h2>
            {message && <p style={styles.message}>{message}</p>}

            {appointments.length === 0 && <p>No confirmed appointments.</p>}

            {appointments.map(apt => (
                <div key={apt.id} style={styles.card}>
                    <h3>Patient: {apt.patient?.name}</h3>
                    <p>{apt.appointmentDate} at {apt.appointmentTime}</p>

                    {apt.symptoms && (
                        <div style={styles.box}>
                            <strong>Patient Symptoms:</strong>
                            <p>{apt.symptoms}</p>
                        </div>
                    )}

                    {apt.preVisitSummary && (
                        <div style={{...styles.box, background:'#ebf8ff'}}>
                            <strong>AI Pre-Visit Summary:</strong>
                            <p>{apt.preVisitSummary}</p>
                        </div>
                    )}

                    <textarea style={styles.textarea}
                              placeholder="Write post-visit notes and prescription here..."
                              value={notes[apt.id] || ''}
                              onChange={e => setNotes({...notes, [apt.id]: e.target.value})} />

                    <button style={styles.button} onClick={() => submitNotes(apt.id)}>
                        Submit Notes & Generate Patient Summary
                    </button>

                    {apt.postVisitSummary && (
                        <div style={{...styles.box, background:'#f0fff4'}}>
                            <strong>AI Post-Visit Summary (sent to patient):</strong>
                            <p>{apt.postVisitSummary}</p>
                        </div>
                    )}
                </div>
            ))}
        </div>
    )
}

const styles = {
    container: { maxWidth:'800px', margin:'0 auto', padding:'2rem' },
    heading: { color:'#2d3748', marginBottom:'1.5rem' },
    card: { background:'white', padding:'1.5rem', borderRadius:'12px',
        marginBottom:'1.5rem', boxShadow:'0 2px 8px rgba(0,0,0,0.08)' },
    box: { background:'#f7fafc', padding:'0.75rem', borderRadius:'6px',
        marginBottom:'0.75rem' },
    textarea: { width:'100%', padding:'0.75rem', height:'100px',
        border:'1px solid #e2e8f0', borderRadius:'8px',
        fontSize:'1rem', boxSizing:'border-box', marginBottom:'0.75rem' },
    button: { padding:'0.75rem 1.5rem', background:'#48bb78', color:'white',
        border:'none', borderRadius:'8px', cursor:'pointer' },
    message: { color:'green', fontWeight:'500', marginBottom:'1rem' }
}