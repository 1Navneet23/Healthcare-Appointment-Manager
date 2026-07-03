import { useState, useRef } from 'react'
import API from '../services/api'

export default function Register() {
    const [form, setForm] = useState({ name:'', email:'', password:'', role:'PATIENT' })
    const [message, setMessage] = useState('')
    const isSubmitting = useRef(false)

    const handleRegister = async () => {
        if (isSubmitting.current) return;
        isSubmitting.current = true;
        try {
            await API.post('/api/auth/register', form)
            setMessage('Registered successfully! You can now login.')
        } catch (e) {
            setMessage('Registration failed. Email may already exist.')
        } finally {
            isSubmitting.current = false;
        }
    }

    return (
        <div style={styles.container}>
            <div style={styles.card}>
                <h2 style={styles.title}>Register</h2>
                {message && <p style={styles.message}>{message}</p>}
                <input style={styles.input} placeholder="Full Name"
                       value={form.name} onChange={e => setForm({...form, name:e.target.value})} />
                <input style={styles.input} placeholder="Email"
                       value={form.email} onChange={e => setForm({...form, email:e.target.value})} />
                <input style={styles.input} placeholder="Password" type="password"
                       value={form.password} onChange={e => setForm({...form, password:e.target.value})} />
                <select style={styles.input} value={form.role}
                        onChange={e => setForm({...form, role:e.target.value})}>
                    <option value="PATIENT">Patient</option>
                    <option value="DOCTOR">Doctor</option>
                    <option value="ADMIN">Admin</option>
                </select>
                <button style={styles.button} onClick={handleRegister}>Register</button>
            </div>
        </div>
    )
}

const styles = {
    container: { display:'flex', justifyContent:'center',
        alignItems:'center', height:'100vh', background:'#f0f4f8' },
    card: { background:'white', padding:'2rem', borderRadius:'12px',
        width:'360px', boxShadow:'0 4px 20px rgba(0,0,0,0.1)' },
    title: { textAlign:'center', marginBottom:'1.5rem', color:'#2d3748' },
    input: { width:'100%', padding:'0.75rem', marginBottom:'1rem',
        border:'1px solid #e2e8f0', borderRadius:'8px',
        fontSize:'1rem', boxSizing:'border-box' },
    button: { width:'100%', padding:'0.75rem', background:'#48bb78',
        color:'white', border:'none', borderRadius:'8px',
        fontSize:'1rem', cursor:'pointer' },
    message: { color:'green', marginBottom:'1rem', textAlign:'center' }
}