/**
 * 防伪码验证规则：长度12-20位
 */
const ANTI_FAKE_CODE_MIN = 12
const ANTI_FAKE_CODE_MAX = 20

export function validateAntiFakeCode(code) {
  if (!code || typeof code !== 'string') {
    return { valid: false, message: '请输入防伪码' }
  }
  const trimmed = code.trim()
  if (trimmed.length < ANTI_FAKE_CODE_MIN) {
    return { valid: false, message: `防伪码长度至少为${ANTI_FAKE_CODE_MIN}位` }
  }
  if (trimmed.length > ANTI_FAKE_CODE_MAX) {
    return { valid: false, message: `防伪码长度不能超过${ANTI_FAKE_CODE_MAX}位` }
  }
  return { valid: true }
}
