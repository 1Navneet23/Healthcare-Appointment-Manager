import { useState } from 'react'
import API from '../services/api'

export default function Login({ onLogin }) {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')

    const handleLogin = async () => {
        try {
            const res = await API.post('/api/auth/login', { email, password })
            localStorage.setItem('token', res.data.token)
            onLogin(res.data.token)
        } catch (e) {
            setError('Invalid email or password')
        }
    }

    return (
        <div style={styles.container}>
            <div style={styles.card}>
                <h2 style={styles.title}>Healthcare Login</h2>
                {error && <p style={styles.error}>{error}</p>}
                <input style={styles.input} placeholder="Email"
                       value={email} onChange={e => setEmail(e.target.value)} />
                <input style={styles.input} placeholder="Password"
                       type="password" value={password}
                       onChange={e => setPassword(e.target.value)} />
                <button style={styles.button} onClick={handleLogin}>Login</button>
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
    button: { width:'100%', padding:'0.75rem', background:'#4299e1',
        color:'white', border:'none', borderRadius:'8px',
        fontSize:'1rem', cursor:'pointer' },
    error: { color:'red', marginBottom:'1rem', textAlign:'center' }
}