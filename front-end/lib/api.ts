const API_BASE_URL = 'https://suhyun444.duckdns.org';

const redirectToLogin = () => {
  document.cookie = `returnUrl=${window.location.pathname}; path=/; max-age=300`;
  window.location.href = `${API_BASE_URL}/oauth2/authorization/google`;
};

const request = async (url: string, options: RequestInit = {}) => {
  const token = localStorage.getItem('accessToken');
  const headers = new Headers(options.headers);

  if (!(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  try{

    const response = await fetch(API_BASE_URL + url, {
      ...options,
      headers,
    });
    
    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('UnAuthorized');
      }
      throw new Error('API request failed');
    }
    return response.json();
  }
  catch (error)
  {
    localStorage.removeItem('accessToken');
    redirectToLogin()
    console.error()
  }

};

export const api = {
  get: (url: string) => request(url),
  
  post: (url: string, body: any) => {
    const isFormData = body instanceof FormData;
    
    return request(url, {
      method: 'POST',
      body: isFormData ? body : JSON.stringify(body)
    });
  },
  patch: (url: string, body: any) => {
    return request(url, {
      method: 'PATCH',
      body: JSON.stringify(body)
    });
  },
  delete: (url: string) => {
    return request(url, {
      method: 'DELETE'
    });
  }
}