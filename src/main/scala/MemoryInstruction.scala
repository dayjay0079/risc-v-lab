import chisel3._
import chisel3.util._

class MemoryInstruction(program: Seq[Int], fpga: Boolean) extends Module {
  val io = IO(new Bundle {
    val pc = Input(UInt(32.W))
    val instruction = Output(UInt(32.W))
  })

  // Define a method to handle unified memory access
  def readMemory(addr: UInt): UInt = {
    if (fpga) {
      IM.asInstanceOf[SyncReadMem[UInt]].read((addr >> 2.U).asUInt)
    } else {
      IM.asInstanceOf[Vec[UInt]]((addr >> 2.U).asUInt)
    }
  }

  // Declare IM as AnyRef (parent type for both SyncReadMem and Vec)
  private val IM: AnyRef = if (fpga) {
    SyncReadMem(1024, UInt(32.W)) // Type: SyncReadMem
  } else {
    RegInit(VecInit(program.map(value => (value & 0xFFFFFFFFL).asUInt(32.W)))) // Type: Vec
  }
  
  // Example usage of readMemory
  if (fpga) {
    io.instruction := readMemory(io.pc)
  } else {
    io.instruction := RegNext(readMemory(io.pc))
  }
}
