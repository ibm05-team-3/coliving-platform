export const SIGNUP_EMAIL_STORAGE_KEY = 'coliving.signupEmail'

export function storeSignupEmail(email) {
  try {
    sessionStorage.setItem(SIGNUP_EMAIL_STORAGE_KEY, email)
  } catch {
    // 저장이 막혀도 이번 이동의 location state 로 주소를 보여 줍니다.
  }
}

export function readSignupEmail() {
  try {
    return sessionStorage.getItem(SIGNUP_EMAIL_STORAGE_KEY)?.trim() ?? ''
  } catch {
    return ''
  }
}
