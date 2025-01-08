import chisel3._
import chisel3.util._

import lib.peripherals.{MemoryMappedUart, StringStreamer}
import lib.peripherals.MemoryMappedUart.UartPins

object Top extends App {
  val FPGA = false
  val MEM_SIZE = 1024
  emitVerilog(
    new Top(FPGA, MEM_SIZE, 50000000, 9600),
    Array("--target-dir", "generated")
  )
}

class Top(fpga: Boolean, mem_size: Int, freq: Int, baud: Int) extends Module {
  val io = IO(new Bundle{

  })
  val IF_stage = new Stage1_IF(fpga, mem_size)
  val ID_stage = new Stage2_ID(fpga)
  val EX_stage = new Stage3_EX(fpga)
  val MEM_stage = new Stage4_MEM(fpga)
  val WB_stage = new Stage5_WB(fpga)

}
