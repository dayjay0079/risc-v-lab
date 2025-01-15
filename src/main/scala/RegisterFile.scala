import chisel3._
import chisel3.util._

class RegisterFile(fpga: Boolean) extends Module {
  val io = IO(new Bundle{
    val rs1 = Input(UInt(5.W))
    val rs2 = Input(UInt(5.W))
    val rd = Input(UInt(5.W))
    val data_in = Input(SInt(32.W))
    val write_enable = Input(Bool())
    val data1 = Output(SInt(32.W))
    val data2 = Output(SInt(32.W))
  })

  // Define methods to handle unified register access
  def readReg(addr: UInt): SInt = {
    if (fpga) {
      regs.asInstanceOf[SyncReadMem[SInt]].read(addr)
    } else {
      regs.asInstanceOf[Vec[SInt]](addr)
    }
  }
  def writeReg(addr: UInt, data: SInt) = {
    if (fpga) {
      regs.asInstanceOf[SyncReadMem[SInt]].write(addr, data)
    } else {
      regs.asInstanceOf[Vec[SInt]](addr) := data
    }
  }

  // Declare register file as AnyRef (parent type for both SyncReadMem and Vec)
  private val regs: AnyRef = if (fpga) {
    SyncReadMem(32, SInt(32.W)) // Type: SyncReadMem
  } else {
    RegInit(VecInit(Seq.fill(32)(0.S(32.W)))) // Type: Vec
  }

  // Reading from registers
  if (fpga) {
    io.data1 := Mux(io.rs1 === 0.U, 0.S, readReg(io.rs1))
    io.data2 := Mux(io.rs2 === 0.U, 0.S, readReg(io.rs2))
  } else {
    io.data1 := RegNext(Mux(io.rs1 === 0.U, 0.S, readReg(io.rs1)))
    io.data2 := RegNext(Mux(io.rs2 === 0.U, 0.S, readReg(io.rs2)))
  }


  // Writing to registers
  when (io.rd =/= 0.U && io.write_enable) {
    writeReg(io.rd, io.data_in)
  }
}
