import http from 'k6/http';

export const options = {
  vus: 10,
  duration: '30s',
  minIterationDuration: '1s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<200'],
  },
};

export default function() {
  http.get('https://test.k6.io');
}
