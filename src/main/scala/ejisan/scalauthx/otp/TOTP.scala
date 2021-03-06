package ejisan.scalauthx.otp

/** A TOTP(Time-based One-time Password Algorithm).
  *
  * {{{
  * import ejisan.scalauthx.otp._
  *
  * val secret: OTPSecretKey = OTPSecretKey()
  * val totp: TOTP = TOTP(OTPHashAlgorithm.SHA1, 6, 30)
  *
  * // Code generation
  * totp(secret) // : String
  * totp(secret, 5) // : Seq[String]
  *
  * // Code validation
  * totp.validate(pin, secret) // : Boolean
  * totp.validate(pin, secret, 5) // : Boolean (Look-ahead 5 more count)
  * }}}
  *
  * @param algorithm the name of selected hashing algorithm
  * @param digits the length of returning OTP pin code string
  * @param period the period of seconds
  */
class TOTP private (val algorithm: OTPHashAlgorithm.Value, val digits: Int, val period: Int) {

  def time(period: Int, baseTimeMillis: Long = System.currentTimeMillis): Long =
    baseTimeMillis / (period * 1000)

  /** Generates OTP pin code with a given user's secret.
    *
    * @param secret the user's secret
    *
    * @return pin code digits
    */
  def apply(secret: OTPSecretKey): String =
    HOTP(algorithm, digits, secret, time(period))

  /** Generates OTP pin code with a given user's secret.
    *
    * @param secret the user's secret
    * @param baseTimeMillis the base time in milli-second
    *
    * @return pin code digits
    */
  def apply(secret: OTPSecretKey, baseTimeMillis: Long): String =
    HOTP(algorithm, digits, secret, time(period, baseTimeMillis))

  /** Generates OTP codes with a given user's secret and a window size.
    *
    * @param secret the user's secret
    * @param window the look-ahead window size
    *
    * @return a sequence of pin code digits
    */
  def apply(secret: OTPSecretKey, window: Int): Seq[String] =
    (-window to window).map(w => HOTP(algorithm, digits, secret, time(period) + w))

  /** Generates OTP codes with a given user's secret and a window size.
    *
    * @param secret the user's secret
    * @param window the look-ahead window size
    * @param baseTimeMillis the base time in milli-second
    *
    * @return a sequence of pin code digits
    */
  def apply(secret: OTPSecretKey, window: Int, baseTimeMillis: Long): Seq[String] =
    (-window to window).map(w => HOTP(algorithm, digits, secret, time(period, baseTimeMillis) + w))

  /** Validates a given OTP pin code with a given user's secret.
    *
    * @param pin the pin code that user generated
    * @param secret the user's secret
    */
  def validate(pin: String, secret: OTPSecretKey): Boolean =
    pin == apply(secret)

  /** Validates a given OTP pin code with a given user's secret.
    *
    * @param pin the pin code that user generated
    * @param secret the user's secret
    * @param baseTimeMillis the base time in milli-second
    */
  def validate(pin: String, secret: OTPSecretKey, baseTimeMillis: Long): Boolean =
    pin == apply(secret, baseTimeMillis)

  /** Validates a given OTP pin code with a given user's secret and a window size.
    *
    * @param pin the pin code that user generated
    * @param secret the user's secret
    * @param window the look-ahead window size
    */
  def validate(pin: String, secret: OTPSecretKey, window: Int): Boolean =
    apply(secret, window).contains(pin)

  /** Validates a given OTP pin code with a given user's secret and a window size.
    *
    * @param pin the pin code that user generated
    * @param secret the user's secret
    * @param window the look-ahead window size
    * @param baseTimeMillis the base time in milli-second
    */
  def validate(pin: String, secret: OTPSecretKey, window: Int, baseTimeMillis: Long): Boolean =
    apply(secret, window, baseTimeMillis).contains(pin)
}

/** Factory for [[ejisan.scalauthx.otp.TOTP]] instances. */
object TOTP {
  /** Creates a TOTP with a given algorithm, digits and period.
    *
    * @param algorithm the name of selected hashing algorithm
    * @param digits the length of returning OTP pin code string
    * @param period the period of seconds
    */
  def apply(algorithm: OTPHashAlgorithm.Value, digits: Int, period: Int): TOTP = {
    require(digits > 0, s"digits must be greater than 0, but it is ($digits)")
    require(period > 0, s"period must be greater than 0, but it is ($period)")
    new TOTP(algorithm, digits, period)
  }
}
