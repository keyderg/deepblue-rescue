import { request } from './client.js'

export const registerTreatment = (payload) =>
    request('/api/treatments', {
        method: 'POST',
        body: JSON.stringify(payload),
    })