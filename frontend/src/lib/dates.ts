// The API uses yyyy-mm-dd. Every screen shows dd-mm-yyyy.

/** yyyy-mm-dd from the API to dd-mm-yyyy for display. */
export function formatDate(isoDate: string | null): string {
  if (!isoDate) return ''
  const [year, month, day] = isoDate.split('-')
  return `${day}-${month}-${year}`
}

/** dd-mm-yyyy typed by the user to yyyy-mm-dd for the API. Returns null if the date isn't valid. */
export function parseDisplayDate(value: string): string | null {
  const match = /^(\d{2})-(\d{2})-(\d{4})$/.exec(value.trim())
  if (!match) return null
  const [, day, month, year] = match
  const date = new Date(Number(year), Number(month) - 1, Number(day))
  const isRealDate =
    date.getFullYear() === Number(year) &&
    date.getMonth() === Number(month) - 1 &&
    date.getDate() === Number(day)
  return isRealDate ? `${year}-${month}-${day}` : null
}

/** Today's date as yyyy-mm-dd, in the user's local time. */
export function todayIso(): string {
  return new Date().toLocaleDateString('en-CA')
}