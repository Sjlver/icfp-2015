object Moves {
  sealed abstract class Move
  case object E extends Move
  case object W extends Move
  case object SE extends Move
  case object SW extends Move
  case object CW extends Move
  case object CCW extends Move
}