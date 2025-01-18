package lib.peripherals

import chisel3._

/** Memory mapped wire of width [[cnt]], that IO has direct access to.
  *
  * The IO is as follows:
  * - [[io.pins_in]] direct access of the peripheral
  * - [[io.pins_out]] periphiral value stored in a register
  *
  */
class MemoryMappedInput(cnt: Int) extends Module {
  assert(cnt <= 32, "Only up to 32 inputs can be controlled through the bus")

  val io = IO(new Bundle {
    /** Peripheral pins */
    val pins_in = Input(UInt(cnt.W))
    val pins_out = Output(UInt(cnt.W))
  })

  io.pins_out := RegNext(io.pins_in)
}
